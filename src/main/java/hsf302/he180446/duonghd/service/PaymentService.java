package hsf302.he180446.duonghd.service;

import hsf302.he180446.duonghd.pojo.Payment;
import hsf302.he180446.duonghd.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
    }
}
