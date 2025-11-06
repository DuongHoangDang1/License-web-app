package hsf302.he180446.duonghd.controller;

import hsf302.he180446.duonghd.pojo.Product;
import hsf302.he180446.duonghd.repository.UserRepository;
import hsf302.he180446.duonghd.service.ProductService;
import hsf302.he180446.duonghd.service.UserWalletService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;



@Controller
public class HomeController {
    private final ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserWalletService walletService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/home")
    public String homePage(Model model, HttpSession ses) {

        return findPaginated(1, "name", "asc", model, ses);
    }

    @GetMapping("/product-detail/{id}")
    public String getProductDetail(@PathVariable Long id, Model model, HttpSession ses) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        model.addAttribute("openModal", true);
        return findPaginated(1, "name", "asc", model, ses);
    }

    @GetMapping("/homepage/{pageNo}")
    public String findPaginated(
            @PathVariable(value = "pageNo") int pageNo,
            @RequestParam(value = "sortField", defaultValue = "name") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            Model model, HttpSession ses) {

        int pageSize = 8;

        Page<Product> page = productService.findPaginated(pageNo, pageSize, sortField, sortDir);
        List<Product> listProducts = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("baseUrl", "homepage");

        model.addAttribute("products", listProducts);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            userRepository.findByUsername(auth.getName()).ifPresent(user -> {
                model.addAttribute("user", user);

                walletService.findByWalletId(user.getId()).ifPresent(wallet ->
                        model.addAttribute("wallet", wallet)
                );
            });
        }

        String msg = (String) ses.getAttribute("successMessage");
        if(msg != null) {
            model.addAttribute("successMessage", msg);
            ses.removeAttribute("successMessage");
        }
        return "home";
    }

    @GetMapping("/search")
    public String listProducts(@RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
                               @RequestParam(value = "sortField", defaultValue = "name") String sortField,
                               @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
                               @RequestParam(value = "keyword", required = false) String keyword, Model model,
                               HttpSession ses) {

        int pageSize = 8;
        Page<Product> page;
        List<Product> listProducts;
        String msg = null;

        if (keyword != null && !keyword.trim().isEmpty()) {
            page = productService.searchByName(keyword, pageNo, pageSize, sortField, sortDir);
            listProducts = page.getContent();

            if (listProducts.isEmpty()) {
                msg = "Không tìm thấy sản phẩm phù hợp";
            }
        } else {
            page = productService.findPaginated(pageNo, pageSize, sortField, sortDir);
            listProducts = page.getContent();
            msg = "Vui lòng nhập từ khóa tìm kiếm";
        }

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword);
        model.addAttribute("products", listProducts);
        model.addAttribute("baseUrl", "search");



        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            userRepository.findByUsername(auth.getName()).ifPresent(user -> {
                model.addAttribute("user", user);

                walletService.findByWalletId(user.getId()).ifPresent(wallet ->
                        model.addAttribute("wallet", wallet)
                );
            });
        }

        if(msg != null) {
            model.addAttribute("successMessage", msg);
            ses.removeAttribute("successMessage");
        }

        return "home";
    }

    @GetMapping("/home2")
    public String homePage2(Model model) {
        model.addAttribute("products", productService.findAll());
        return "home2";
    }
}
