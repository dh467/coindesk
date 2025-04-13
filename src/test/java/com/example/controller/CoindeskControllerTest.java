package com.example.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.model.CurrencyMap;
import com.example.repository.CurrencyMapRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CoindeskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CurrencyMapRepository repository;

  @BeforeEach
  void setUp() {
    repository.deleteAll();
    repository.save(new CurrencyMap("bitcoin", "比特幣"));
    repository.save(new CurrencyMap("ethereum", "以太幣"));
    repository.save(new CurrencyMap("dogecoin", "狗狗幣"));
  }

  // --------------正常流程測試--------------
  @Test
  @DisplayName("1. 測試呼叫查詢幣別對應表資料API，並顯示其內容。")
  void testGetCurrency() throws Exception {
    mockMvc.perform(get("/api/v1/coindesk/currencies/bitcoin"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.currency", is("bitcoin")))
        .andExpect(jsonPath("$.chineseName", is("比特幣")))
        .andDo(print());
  }

  @Test
  @DisplayName("2. 測試呼叫新增幣別對應表資料API。")
  void testAddCurrency() throws Exception {
    String json = "{" + "\"currency\": \"binancecoin\"," + "\"chineseName\": \"幣安幣\"}";

    mockMvc.perform(post("/api/v1/coindesk/currencies")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isCreated())
        .andDo(print());
  }

  @Test
  @DisplayName("3. 測試呼叫更新幣別對應表資料API，並顯示其內容。")
  void testUpdateCurrency() throws Exception {
    String json = "{" + "\"chineseName\": \"比特幣（更新）\"}";

    mockMvc.perform(put("/api/v1/coindesk/currencies/bitcoin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.chineseName", containsString("（更新）")))
        .andDo(print());
  }

  @Test
  @DisplayName("4. 測試呼叫刪除幣別對應表資料API。")
  void testDeleteCurrency() throws Exception {
    mockMvc.perform(delete("/api/v1/coindesk/currencies/bitcoin"))
        .andExpect(status().isNoContent())
        .andDo(print());
  }

  @Test
  @DisplayName("5. 測試呼叫 coinGecko API，並顯示其內容。")
  void testGetCoingeckoData() throws Exception {
    mockMvc.perform(get("/api/v1/coindesk/coingecko/raw"))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  @DisplayName("6. 測試呼叫資料轉換的API，並顯示其內容。")
  void testGetAllCurrencies() throws Exception {
    mockMvc.perform(get("/api/v1/coindesk/currencies"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", not(empty())))
        .andExpect(jsonPath("$[*].currency", everyItem(notNullValue())))
        .andExpect(jsonPath("$[*].rate", everyItem(notNullValue())))
        .andExpect(jsonPath("$[*].updateAt", everyItem(notNullValue())))
        .andDo(print());
  }



  // --------------異常流程測試--------------
  @Test
  @DisplayName("7. 測試查詢不存在幣別（回傳 404）")
  void testGetCurrency_notFound() throws Exception {
    mockMvc.perform(get("/api/v1/coindesk/currencies/bitccccoin"))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("8. 測試更新不存在幣別（回傳 404）")
  void testUpdateCurrency_notFound() throws Exception {
    String json = "{" + "\"chineseName\": \"比特幣（更新）\"}";

    mockMvc.perform(put("/api/v1/coindesk/currencies/bitccccoin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("9. 測試刪除不存在幣別（回傳 404）")
  void testDeleteCurrency_notFound() throws Exception {
    mockMvc.perform(delete("/api/v1/coindesk/currencies/bitccccoin"))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("10. 測試新增缺少欄位（回傳 400）")
  void testAddCurrency_missingField() throws Exception {
    String json = "{" + "\"currency\": \"\"," + "\"chineseName\": \"幣安幣\"}"; // 必要欄位value為空
//    String json = "{" + "\"currency\": \"binancecoin\"" + "}"; // 必要欄位未傳入
//    String json = "{}"; // 必要欄位皆空

    mockMvc.perform(post("/api/v1/coindesk/currencies")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.errors", everyItem(containsString("is required"))))
        .andDo(print());
  }

  @Test
  @DisplayName("11. 測試新增傳送未定義欄位（回傳 400）")
  void testAddCurrency_unknownField() throws Exception {
    String json = "{" + "\"currency\": \"binancecoin\"," + "\"ccc\": \"binanccccecoin\","
        + "\"chineseName\": \"幣安幣\"}";

    mockMvc.perform(post("/api/v1/coindesk/currencies")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Request contains undefined field")))
        .andDo(print());
  }

  @Test
  @DisplayName("12. 測試新增已存在幣別（回傳 409）")
  void testAddCurrency_duplicate() throws Exception {
    String json = "{ \"currency\": \"binancecoin\", \"chineseName\": \"幣安幣\" }";

    // 第一次新增成功
    mockMvc.perform(post("/api/v1/coindesk/currencies")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json));

    // 第二次新增，觸發新增已存在幣別異常情境
    mockMvc.perform(post("/api/v1/coindesk/currencies")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message", containsString("already exists")))
        .andDo(print());
  }
}
