package com.k_int.pushKb.model;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoKBRecordDeserializer extends StdDeserializer<ArrayList<String>> {
   public GoKBRecordDeserializer() { 
      this(null); 
   } 
   public GoKBRecordDeserializer(Class<String> t) { 
      super(t); 
   } 
  
   @Override 
   public ArrayList<String> deserialize(JsonParser parser, DeserializationContext context) 
      throws IOException, JsonProcessingException { 
      log.info("WHAT IS THIS? {}", parser.getText());

      ArrayList<String> output = new ArrayList<String>();
      output.add("test1");
      output.add("test2");
      return output;
   }
}
