# Coindesk API

本專案使用 Spring Boot 建立一套 RESTful API，整合外部API並實作幣別對應資料維護功能，支援基本 CRUD 與資料轉換功能，並以 H2 資料庫儲存幣別對應中文名稱。

## 🔁 資料來源與說明

**原指定為 Coindesk API（已下線），改以功能相近的 Coingecko API 實作**

使用 API：[`https://api.coingecko.com/api/v3/coins/markets?vs_currency=twd&ids=bitcoin,ethereum,dogecoin`]

因 Coingecko 提供幣別數量龐大，本專案預設初始化三筆資料：`bitcoin`、`ethereum`、`dogecoin` 作為示範用途。


## ✅ 專案環境

- Java 版本：Java 8
- 架構：Spring Boot 2.7.14
- 建構工具：Maven
- DB：H2（記憶體資料庫，搭配 Spring Data JPA / Hibernate）
- 測試框架：SpringBootTest + MockMvc

## 📌 專案目的

1. 呼叫 Coingecko API，取得幣別市價資訊。
2. 建立幣別對應中文名稱資料表，並提供以下 API：
   - 查詢幣別對應資訊
   - 新增幣別對應資料
   - 修改幣別對應資料
   - 刪除幣別對應資料
3. 提供轉換後的新 API，顯示更新時間與幣別資訊（幣別 / 中文名稱 / 匯率）。

## 📂 API 說明

| 功能                | 方法     | 路徑                                       | 說明                                 |
|-------------------|--------|------------------------------------------|------------------------------------|
| 查詢幣別對應資料（全部）      | GET    | `/api/v1/coindesk/currencies`            | 取得所有幣別對應資料，顯示更新時間 + 幣別 + 中文名稱 + 匯率 |
| 查詢幣別對應資料（單筆）      | GET    | `/api/v1/coindesk/currencies/{currency}` | 根據幣別 ID 查詢對應資訊                     |
| 新增幣別對應資料          | POST   | `/api/v1/coindesk/currencies`            | 建立新的幣別與中文名稱                        |
| 更新幣別對應資料          | PUT    | `/api/v1/coindesk/currencies/{currency}` | 修改幣別對應的中文名稱                        |
| 刪除幣別對應資料          | DELETE | `/api/v1/coindesk/currencies/{currency}` | 刪除指定幣別資料                           |
| 查詢原始 CoinGecko 資料 | GET    | `/api/v1/coindesk/coingecko/raw`         | 取得整批原始 JSON 資料                     |

### 📋 API Request 範例

#### 🟢 新增幣別（POST `/api/v1/coindesk/currencies`）

**Request Body**：
```json
{
  "currency": "binancecoin",
  "chineseName": "幣安幣"
}
```

**成功時回傳**：
```http
201 Created
```

#### 🟡 更新幣別（PUT `/api/v1/coindesk/currencies/{currency}`）

**Request Body**：
```json
{
  "chineseName": "比特幣（更新）"
}
```

**成功時回傳**：
```json
{
  "currency": "bitcoin",
  "chineseName": "比特幣（更新）",
  "createAt": "2025-04-12T08:00:00"
}
```

## 💾 資料表建立語法（H2）

```sql
CREATE TABLE currency_map (
  currency VARCHAR(50) PRIMARY KEY,
  chinese_name VARCHAR(50) NOT NULL,
  create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

> 資料欄位說明：
> - `currency`：幣別代號，主鍵
> - `chinese_name`：中文名稱，不可為 NULL
> - `create_at`：建立時間 (此欄位僅用於記錄，API 不開放修改)

## 🧪 單元測試說明

採用 SpringBootTest + MockMvc 撰寫單元測試驗證 RESTful 行為，包含 status code、回傳 JSON 結構與內容，涵蓋正常流程與異常流程，主要測試項目包含：

### ✅ 正常流程測試

- 測試呼叫查詢幣別對應表資料API，並顯示其內容。
- 測試呼叫新增幣別對應表資料API。
- 測試呼叫更新幣別對應表資料API，並顯示其內容。
- 測試呼叫刪除幣別對應表資料API。
- 測試呼叫 coinGecko API，並顯示其內容。
- 測試呼叫資料轉換的API，並顯示其內容。

### ❌ 錯誤情境測試

- 測試查詢不存在幣別（回傳 404）
- 測試更新不存在幣別（回傳 404）
- 測試刪除不存在幣別（回傳 404）
- 測試新增缺少欄位（回傳 400）
- 測試新增傳送未定義欄位（回傳 400）
- 測試新增已存在幣別（回傳 409）

## 💾 H2 資料庫使用說明

- 專案啟動後會自動建立記憶體資料庫。
- 初始會透過 SQL 或程式（`@PostConstruct`）建立 3 筆幣別資料。
- 測試中透過 `@Sql` 或 `@BeforeEach` 重設測試資料狀態。

## 📝 注意事項

- 僅展示 API 功能與資料轉換流程，未接前端畫面。

## 📎 參考文件

- [CoinGecko API Docs](https://www.coingecko.com/en/api/documentation)
- [Spring Boot 官方文件](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)

---

## 🧑‍ 作者：林昱伸 Mark