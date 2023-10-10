package com.hcmus.mentor.backend.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class APIResponse<T extends Object> {
  private static final Integer SUCCESS_CODE = 200;

  private boolean success;
  private String message;

  private T data;

  private Integer returnCode;

  public APIResponse(boolean success, String message, Integer returnCode) {
    this.success = success;
    this.message = message;
    this.returnCode = returnCode;
  }

  public APIResponse(T data, Integer returnCode) {
    this(true, null, returnCode);
    this.data = data;
  }

  public APIResponse(T data, Integer returnCode, String message) {
    this(true, message, returnCode);
    this.data = data;
  }

  //    public static ApiResponse success(Integer returnCode) {
  //        return new ApiResponse(true, null, returnCode);
  //    }

  public static APIResponse success(Object data) {
    return new APIResponse(data, SUCCESS_CODE);
  }

  public static APIResponse notFound(Integer returnCode) {
    return new APIResponse(false, "Not found", returnCode);
  }
}
