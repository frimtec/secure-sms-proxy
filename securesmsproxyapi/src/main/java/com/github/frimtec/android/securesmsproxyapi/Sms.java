package com.github.frimtec.android.securesmsproxyapi;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Sms {

  private final String number;
  private final String text;

  public Sms(String number, String text) {
    this.number = number;
    this.text = text;
  }

  public String getNumber() {
    return number;
  }

  public String getText() {
    return text;
  }

  @NonNull
  @Override
  public String toString() {
    return "Sms{" +
        "number='" + number + '\'' +
        ", text='" + text + '\'' +
        '}';
  }

  public String toJson() {
      return toJsonObject(this).toString();
  }

  public static String toJsonArray(List<Sms> smsList) {
    return new JSONArray(smsList.stream().map(Sms::toJsonObject).collect(Collectors.toList())).toString();
  }

  private static JSONObject toJsonObject(Sms sms) {
    try {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("number", sms.getNumber());
      jsonObject.put("text", sms.getText());
      return jsonObject;
    } catch (JSONException e) {
      throw new IllegalStateException("Cannot generate JSON string", e);
    }
  }

  public static Sms fromJson(String json) {
    try {
      return toSms(new JSONObject(json));
    } catch (JSONException e) {
      throw new IllegalStateException("Cannot generate JSON string", e);
    }
  }

  public static List<Sms> fromJsonArray(String jsonArray) {
    try {
      JSONArray array = new JSONArray(jsonArray);
      List<Sms> result = new ArrayList<>();
      for (int i = 0; i < array.length(); i++) {
        result.add(toSms(array.getJSONObject(i)));
      }
      return result;
    } catch (JSONException e) {
      throw new IllegalStateException("Cannot generate JSON string", e);
    }
  }

  private static Sms toSms(JSONObject jsonObject) throws JSONException {
    return new Sms(jsonObject.getString("number"), jsonObject.getString("text"));
  }
}
