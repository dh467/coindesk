package com.example.dto;


import javax.validation.constraints.NotBlank;

public class UpdateCurrencyRequest {

  @NotBlank(message = "is required")
  private String chineseName;

  public String getChineseName() {
    return chineseName;
  }

  public void setChineseName(String chineseName) {
    this.chineseName = chineseName;
  }

}
