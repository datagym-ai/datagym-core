package com.eforce21.lib.bin.file.example;

import com.eforce21.lib.bin.file.service.BinFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

/**
 * Example of how a controller should look like.
 * In real world scenarios you might want to add additional security
 * checks and provide methods always in context of related entities.
 * That's why a global FileController doesn't make that much sense.
 */
// @Controller
// @RequestMapping("/api/abc")
public class BinFileControllerExample {

    @Autowired
    private BinFileService binFileService;

    @GetMapping("/product/{productId}/file/{fileId}")
    public void createProductFile(HttpServletRequest req, HttpServletResponse res, @PathVariable("productId") long productId, @PathVariable("fileId") long fileId) throws IOException {
        // service.streamProductFile(productId, fileId, new BinFileConsumerHttp(res));
    }

    @PostMapping("/product/{productId}/file")
    public void createProductFile(HttpServletRequest req, HttpServletResponse res, @PathVariable("productId") long productId) throws IOException {
        String filename = new String(Base64.getDecoder().decode(req.getHeader("X-filename")));
        // service.createProductFile(productId, filename, req.getInputStream());
    }

    @DeleteMapping("/product/{productId}/file/{fileId}")
    public void deleteProductFile(@PathVariable("productId") long productId, @PathVariable("fileId") long fileId) {
        // service.deleteProductFile(productId, fileId);
    }

}
