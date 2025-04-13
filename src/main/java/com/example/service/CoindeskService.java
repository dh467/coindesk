package com.example.service;

import static com.example.common.ErrorMessages.COINGECKO_NO_DATA;
import static com.example.common.ErrorMessages.COINGECKO_PARSE_ERROR;
import static com.example.common.ErrorMessages.TABLE_NO_DATA;

import com.example.dto.CurrencyInfoDTO;
import com.example.exception.ResourceNotFoundException;
import com.example.model.CurrencyMap;
import com.example.repository.CurrencyMapRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 原指定為 Coindesk API，但其已下線，故改以功能相近 CoinGecko 提供的開源 API 實作：
 * https://api.coingecko.com/api/v3/coins/markets?vs_currency=twd&ids=...
 */
@Service
public class CoindeskService {

  private static final Logger log = LoggerFactory.getLogger(CoindeskService.class);

  private final RestTemplate restTemplate;
  private final CurrencyMapRepository repository;
  private final ObjectMapper objectMapper;

  public CoindeskService(RestTemplate restTemplate, CurrencyMapRepository repository,
      ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.repository = repository;
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  public void initDefaultCurrencies() {
    if (repository.count() == 0) {
      repository.saveAll(Arrays.asList(
          new CurrencyMap("bitcoin", "比特幣"),
          new CurrencyMap("ethereum", "以太幣"),
          new CurrencyMap("dogecoin", "狗狗幣")
      ));
    }
  }

  public String getCoingeckoData() {
    List<String> currencyList = repository.findAll().stream()
        .map(CurrencyMap::getCurrency)
        .collect(Collectors.toList());
    if (currencyList.isEmpty()) {
      throw new ResourceNotFoundException(TABLE_NO_DATA);
    }
    String joinedIds = String.join(",", currencyList);
    String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=twd&ids=" + joinedIds;
    log.info("Fetching data from CoinGecko API: {}", url);
    String coingeckoData = restTemplate.getForObject(url, String.class);
    try {
      JsonNode jsonCoingeckoData = objectMapper.readTree(coingeckoData);
      if (jsonCoingeckoData.isArray() && jsonCoingeckoData.isEmpty()) {
        throw new ResourceNotFoundException(COINGECKO_NO_DATA);
      }
    } catch (Exception e) {
      log.error("Failed to parse CoinGecko response", e);
      throw new RuntimeException(COINGECKO_PARSE_ERROR, e);
    }

    return coingeckoData;
  }

  public List<CurrencyInfoDTO> getAllCurrencies() {
    String rawData = getCoingeckoData();
    JsonNode jsonNode;
    try {
      jsonNode = objectMapper.readTree(rawData);
    } catch (Exception e) {
      log.error("Failed to parse CoinGecko response", e);
      throw new RuntimeException(COINGECKO_PARSE_ERROR, e);
    }

    Map<String, String> currencyToChineseName = repository.findAll().stream()
        .collect(Collectors.toMap(CurrencyMap::getCurrency, CurrencyMap::getChineseName));

    List<CurrencyInfoDTO> result = new ArrayList<>();
    for (JsonNode node : jsonNode) {
      String lastUpdated = formatToTaipeiTime(node.path("last_updated").asText());
      String id = node.path("id").asText();
      String chineseName = currencyToChineseName.getOrDefault(id, "");
      BigDecimal rate = node.path("current_price").decimalValue();

      CurrencyInfoDTO dto = new CurrencyInfoDTO();
      dto.setUpdateAt(lastUpdated);
      dto.setCurrency(id);
      dto.setChineseName(chineseName);
      dto.setRate(rate);

      result.add(dto);
    }
    result.sort(Comparator.comparing(CurrencyInfoDTO::getCurrency));

    return result;
  }

  private String formatToTaipeiTime(String lastUpdated) {
    return ZonedDateTime.parse(lastUpdated)
        .withZoneSameInstant(ZoneId.of("Asia/Taipei"))
        .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
  }

  public Optional<CurrencyMap> findByCurrency(String currency) {
    return repository.findById(currency);
  }

  public CurrencyMap insert(CurrencyMap currencyMap) {
    String currency = currencyMap.getCurrency();
    if (repository.existsById(currency)) {
      throw new DuplicateKeyException("Currency '" + currency + "' already exists.");
    }
    return repository.save(currencyMap);
  }

  public CurrencyMap update(String currency, String chineseName) {
    CurrencyMap currencyData = repository.findById(currency)
        .orElseThrow(() -> new ResourceNotFoundException("Currency '" + currency + "' not found."));
    currencyData.setChineseName(chineseName);
    return repository.save(currencyData);
  }

  public void delete(String currency) {
    log.warn("Deleting currency information : [{}]", currency);
    repository.findById(currency)
        .orElseThrow(() -> new ResourceNotFoundException("Currency '" + currency + "' not found."));
    repository.deleteById(currency);
  }
}
