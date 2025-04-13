package com.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "currency_map")
@JsonIgnoreProperties(ignoreUnknown = false)
public class CurrencyMap {


  @Id
  @Column(name = "currency", nullable = false, unique = true)
  @NotBlank(message = "is required")
  private String currency;

  @Column(name = "chinese_name", nullable = false)
  @NotBlank(message = "is required")
  private String chineseName;

  @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
  @Column(name = "create_at", updatable = false)
  @CreationTimestamp
  private LocalDateTime createAt;

  @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
  @Column(name = "update_at")
  @UpdateTimestamp
  private LocalDateTime updateAt;

  public CurrencyMap() {
  }

  public CurrencyMap(String currency, String chineseName) {
    this.currency = currency;
    this.chineseName = chineseName;
  }

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

  public LocalDateTime getCreateAt() {
    return createAt;
  }

  public void setCreateAt(LocalDateTime createAt) {
    this.createAt = createAt;
  }

  public LocalDateTime getUpdateAt() {
    return updateAt;
  }

  public void setUpdateAt(LocalDateTime updateAt) {
    this.updateAt = updateAt;
  }
}
