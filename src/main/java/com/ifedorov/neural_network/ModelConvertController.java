package com.ifedorov.neural_network;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class ModelConvertController {

        @RequestMapping(value = "/fromXlsx", method = RequestMethod.POST, consumes = "multipart/form-data")
        public ResponseEntity homePage(@RequestParam("file") MultipartFile file) {
                try(InputStream is = file.getInputStream()) {
                        return ResponseEntity.ok(Model.load(is).toJsonModel().toJson());
                } catch (IOException e) {
                        throw new RuntimeException(e);
                }
        }

        @RequestMapping(value = "/toXlsx", method = RequestMethod.POST, consumes = "multipart/form-data", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public /*@ResponseBody byte[]*/ void  toXlsx(@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
                try(InputStream is = file.getInputStream()) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        JsonModel.fromJson(is).toModel().saveTo(byteArrayOutputStream);
                        Files.write(Paths.get("C:\\sources\\neural_network\\build\\123.xlsx"), byteArrayOutputStream.toByteArray());
                        response.reset();
//                        response.setBufferSize(DEFAULT_BUFFER_SIZE);
                        response.getOutputStream().write(byteArrayOutputStream.toByteArray());
//                        return byteArrayOutputStream.toByteArray();
                } catch (IOException e) {
                        throw new RuntimeException(e);
                }
        }
}
