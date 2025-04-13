package com.example.dto;

import java.math.BigDecimal;

public class CurrencyInfoDTO {
  private String currency;
  private String chineseName;
  private BigDecimal rate;
  private String updateAt; // yyyy/MM/dd HH:mm:ss

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getChineseName() {
    return chineseName;
  }

  public void setChineseName(String chineseName) {
    this.chineseName = chineseName;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public void setRate(BigDecimal rate) {
    this.rate = rate;
  }

  public String getUpdateAt() {
    return updateAt;
  }

  public void setUpdateAt(String updateAt) {
    this.updateAt = updateAt;
  }
}
