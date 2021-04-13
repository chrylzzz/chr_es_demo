//package com.chryl.config;
//
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.elasticsearch.client.Client;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
//import org.springframework.data.elasticsearch.core.EntityMapper;
//
//import java.io.IOException;
//import java.net.UnknownHostException;
//
//
///**
// * 时间处理:存时间问题未解决
// * Created by Chr.yl on 2021/4/9.
// *
// * @author Chr.yl
// */
//@Configuration
//public class ElasticsearchConfig {
//    @Bean
//    public ElasticsearchTemplate elasticsearchTemplate(Client client) throws UnknownHostException {
//        return new ElasticsearchTemplate(client, new ElasticCustomEntityMapper());
//    }
//
//    private class ElasticCustomEntityMapper implements EntityMapper {
//
//        private ObjectMapper mapper;
//
//        @Autowired
//        private ElasticCustomEntityMapper() {
//            this.mapper = new ObjectMapper();
//            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//            mapper.registerModule(new JavaTimeModule());
//        }
//
//        @Override
//        public String mapToString(Object object) throws IOException {
//            return mapper.writeValueAsString(object);
//        }
//
//        @Override
//        public <T> T mapToObject(String source, Class<T> clazz) throws IOException {
//            return mapper.readValue(source, clazz);
//        }
//    }
//}
