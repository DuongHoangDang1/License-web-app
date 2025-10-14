package com.example.demo.service;

import com.example.demo.pojo.DepositTransaction;
import com.example.demo.repository.DepositTransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VnpayService {
    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.pay-url}")
    private String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    private final UserWalletService walletRepository;
    private final DepositTransactionRepository transactionRepository;

    public VnpayService(UserWalletService walletRepository,
                        DepositTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public String createPaymentUrl(Long amount, HttpServletRequest request) {
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String vnp_OrderInfo = "Nap tien vao vi";
        String vnp_IpAddr = request.getRemoteAddr();
        String vnp_CreateDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Long userId = getCurrentUserId();

        DepositTransaction tx = new DepositTransaction();
        tx.setTxnRef(vnp_TxnRef);
        tx.setAmount(amount);
        tx.setUserId(userId);
        tx.setStatus(DepositTransaction.Status.PENDING);
        transactionRepository.save(tx);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String value = vnp_Params.get(fieldName);
            if (value != null && !value.isEmpty()) {
                String encodedValue = URLEncoder.encode(value, StandardCharsets.US_ASCII);
                hashData.append(fieldName).append('=').append(encodedValue).append('&');
                query.append(fieldName).append('=').append(encodedValue).append('&');
            }
        }
        hashData.deleteCharAt(hashData.length() - 1);
        query.deleteCharAt(query.length() - 1);

        String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);


        return vnp_PayUrl + "?" + query.toString();

    }

    @Transactional
    public boolean handleVnpayReturn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        for (Enumeration<String> enums = request.getParameterNames(); enums.hasMoreElements();) {
            String paramName = enums.nextElement();
            params.put(paramName, request.getParameter(paramName));
        }

        String vnp_SecureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String value = params.get(fieldName);
            if (value != null && !value.isEmpty()) {
                hashData.append(fieldName).append('=').append(value).append('&');
            }
        }
        hashData.deleteCharAt(hashData.length() - 1);
        String signValue = hmacSHA512(vnp_HashSecret, hashData.toString());
        if (!signValue.equals(vnp_SecureHash)) {
            return false;
        }

        String txnRef = params.get("vnp_TxnRef");
        Optional<DepositTransaction> txOpt = transactionRepository.findByTxnRef(txnRef);
        if (txOpt.isEmpty()) return false;

        DepositTransaction tx = txOpt.get();
        if (tx.getStatus() == DepositTransaction.Status.SUCCESS) {
            return true;
        }

        if ("00".equals(params.get("vnp_ResponseCode"))) {
            walletRepository.topUpWallet(tx.getUserId(), tx.getAmount());

            tx.setStatus(DepositTransaction.Status.SUCCESS);
            transactionRepository.save(tx);
            return true;
        } else {
            tx.setStatus(DepositTransaction.Status.FAILED);
            transactionRepository.save(tx);
            return false;
        }
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC SHA512", e);
        }
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        return Long.parseLong(auth.getName());
        return 2L;
    }

}
