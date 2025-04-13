package com.example.controller;

import com.example.dto.CurrencyInfoDTO;
import com.example.model.CurrencyMap;
import com.example.service.CoindeskService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 幣別資訊查詢與轉換 API，實作基於 CoinGecko 公開 API。
 */
@RestController
@RequestMapping("/api/v1/coindesk")
public class CoindeskController {

  private final CoindeskService coindeskService;

  public CoindeskController(CoindeskService coindeskService) {
    this.coindeskService = coindeskService;
  }

  /**
   * 從 Coingecko API 取得原始幣別資料 JSON 字串。
   *
   * @return Coingecko 原始回應資料
   */
  @GetMapping("/coingecko/raw")
  public ResponseEntity<String> getCoingeckoData() {
    return ResponseEntity.ok(coindeskService.getCoingeckoData());
  }

  /**
   * 查詢並轉換所有幣別資訊（含匯率與更新時間）。
   *
   * @return 幣別資訊清單
   */
  @GetMapping("/currencies")
  public ResponseEntity<List<CurrencyInfoDTO>> getAllCurrencies() {
    return ResponseEntity.ok(coindeskService.getAllCurrencies());
  }

  /**
   * 查詢指定幣別對應表資料。
   *
   * @param currency 幣別 ID（如：bitcoin）
   * @return 幣別對應資料，若不存在則回傳 404
   */
  @GetMapping("/currencies/{currency}")
  public ResponseEntity<CurrencyMap> getCurrency(@PathVariable String currency) {
    return coindeskService.findByCurrency(currency)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * 新增幣別對應表資料。
   *
   * @param currencyMap 幣別對應物件，包含幣別 ID 與中文名稱
   * @return 建立成功回應（201 Created）
   */
  @PostMapping("/currencies")
  public ResponseEntity<CurrencyMap> addCurrency(@RequestBody @Valid CurrencyMap currencyMap) {
    coindeskService.insert(currencyMap);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /**
   * 修改指定幣別的中文名稱。
   *
   * @param currency    幣別 ID（如：bitcoin）
   * @param chineseName 要更新的中文名稱
   * @return 更新後的幣別對應資料
   */
  @PutMapping("/currencies/{currency}")
  public ResponseEntity<CurrencyMap> updateCurrency(
      @PathVariable String currency,
      @RequestBody @Valid String chineseName) {
    return ResponseEntity.ok(coindeskService.update(currency, chineseName));
  }

  /**
   * 刪除指定幣別對應表資料。
   *
   * @param currency 幣別 ID（如：bitcoin）
   * @return 無內容（204 No Content）
   */
  @DeleteMapping("/currencies/{currency}")
  public ResponseEntity<String> deleteCurrency(@PathVariable String currency) {
    coindeskService.delete(currency);
    return ResponseEntity.noContent().build();
  }
}


