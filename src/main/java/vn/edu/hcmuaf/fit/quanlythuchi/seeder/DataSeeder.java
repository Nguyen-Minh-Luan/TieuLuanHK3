package vn.edu.hcmuaf.fit.quanlythuchi.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.*;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.*;
import vn.edu.hcmuaf.fit.quanlythuchi.util.MoneyToWordsConverter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DataSeeder - Sinh dữ liệu mẫu cho hệ thống Quản lý Thu Chi.
 * Tự động chạy khi ứng dụng khởi động (CommandLineRunner).
 * Chỉ seed khi database còn trống để tránh tạo trùng lặp mỗi lần restart.
 *
 * Sinh ra:
 * - 5 User         (password được mã hoá bằng BCrypt, đúng theo AuthServiceImpl)
 * - 6 Category     (INCOME / EXPENSE)
 * - 3 Fund
 * - 5 Partner
 * - 5 Debt         (đầy đủ thông tin)
 * - 30 Transaction (Sử dụng đúng status: ACTIVE/CANCELLED và warningLevel: NORMAL/WARNING/CRITICAL)
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AuthRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FundRepository fundRepository;
    private final PartnerRepository partnerRepository;
    private final DebtRepository debtRepository;
    private final TransactionRepository transactionRepository;

    private final BCryptPasswordEncoder hashMachine = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        if (transactionRepository.count() > 0) {
            System.out.println("--- [DataSeeder] Dữ liệu đã tồn tại, bỏ qua seeding ---");
            return;
        }
        System.out.println("--- [DataSeeder] Bắt đầu seeding dữ liệu mẫu ---");

        List<User> users = seedUsers();
        List<Category> categories = seedCategories();
        List<Fund> funds = seedFunds();
        List<Partner> partners = seedPartners();
        List<Debt> debts = seedDebts(users, categories, partners);
        seedTransactions(users, categories, funds, partners, debts);

        System.out.println("--- [DataSeeder] Seeding hoàn tất: "
                + users.size() + " user, "
                + categories.size() + " category, "
                + funds.size() + " fund, "
                + partners.size() + " partner, "
                + debts.size() + " debt, 30 transaction ---");
    }

    // ================================================================
    // USERS
    // ================================================================
    private List<User> seedUsers() {
        List<User> list = new ArrayList<>();

        list.add(buildUser("admin", "Admin@123", "Nguyễn Văn Quản Trị",
                "admin@quanlythuchi.vn", 1, "ACTIVE", date(2025, 1, 1)));
        list.add(buildUser("ketoan01", "KeToan@123", "Trần Thị Kế Toán",
                "ketoan01@quanlythuchi.vn", 2, "ACTIVE", date(2025, 2, 15)));
        list.add(buildUser("thuchi01", "ThuChi@123", "Lê Văn Thu Chi",
                "thuchi01@quanlythuchi.vn", 3, "ACTIVE", date(2025, 3, 10)));
        list.add(buildUser("22130154", "123", "Nguyen Minh Luan",
                "22130154@quanlythuchi.vn", 0, "ACTIVE", date(2025, 3, 10)));

        list.add(buildUser("tonghop01", "TongHop@123", "Phạm Thị Tổng Hợp",
                "tonghop01@quanlythuchi.vn", 4, "ACTIVE", date(2025, 4, 1)));

        return userRepository.saveAll(list);
    }

    private User buildUser(String username, String rawPassword, String fullName,
                           String email, Integer role, String status, Date createdAt) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(hashMachine.encode(rawPassword));
        u.setFullName(fullName);
        u.setEmail(email);
        u.setRole(role);
        u.setStatus(status);
        u.setIsDeleted(false);
        u.setCreated_at(createdAt);
        u.setUpdate_at(createdAt);
        return u;
    }

    // ================================================================
    // CATEGORIES
    // ================================================================
    private List<Category> seedCategories() {
        List<Category> list = new ArrayList<>();

        list.add(buildCategory("Lương nhân viên", CategoryType.INCOME,
                "Thu nhập từ lương hàng tháng", 0.0, 0));
        list.add(buildCategory("Doanh thu bán hàng", CategoryType.INCOME,
                "Doanh thu từ hoạt động bán hàng, cung cấp dịch vụ", 0.0, 10));
        list.add(buildCategory("Thu nhập khác", CategoryType.INCOME,
                "Các khoản thu nhập phát sinh khác", 0.0, 0));
        list.add(buildCategory("Chi phí văn phòng", CategoryType.EXPENSE,
                "Chi phí văn phòng phẩm, thuê mặt bằng", 5_000_000.0, 0));
        list.add(buildCategory("Chi phí marketing", CategoryType.EXPENSE,
                "Chi phí quảng cáo, truyền thông", 10_000_000.0, 0));
        list.add(buildCategory("Mua nguyên vật liệu", CategoryType.EXPENSE,
                "Chi phí nhập nguyên vật liệu, hàng hoá", 20_000_000.0, 0));

        return categoryRepository.saveAll(list);
    }

    private Category buildCategory(String name, CategoryType type, String description,
                                   Double budgeting, Integer tax) {
        return Category.builder()
                .name(name)
                .type(type)
                .description(description)
                .budgeting(java.math.BigDecimal.valueOf(budgeting))
                .tax(tax)
                .isDeleted(false)
                .build();
    }

    // ================================================================
    // FUNDS
    // ================================================================
    private List<Fund> seedFunds() {
        List<Fund> list = new ArrayList<>();

        list.add(buildFund("Quỹ tiền mặt", "CASH", 50_000_000.0, 50_000_000.0,
                "ACTIVE", "QUY-001", "Quỹ tiền mặt tại văn phòng"));
        list.add(buildFund("Tài khoản Vietcombank", "BANK", 200_000_000.0, 200_000_000.0,
                "ACTIVE", "QUY-002", "Tài khoản thanh toán chính tại Vietcombank"));
        list.add(buildFund("Tài khoản Techcombank", "BANK", 80_000_000.0, 80_000_000.0,
                "ACTIVE", "QUY-003", "Tài khoản phụ dùng cho chi tiêu vận hành"));

        return fundRepository.saveAll(list);
    }

    private Fund buildFund(String name, String type, Double initialBalance, Double currentBalance,
                           String status, String code, String note) {
        Fund f = new Fund();
        f.setName(name);
        f.setType(type);
        f.setInitialBalance(initialBalance);
        f.setCurrentBalance(currentBalance);
        f.setStatus(status);
        f.setCode(code);
        f.setNote(note);
        f.setIsDeleted(false);
        f.setCreated_at(date(2025, 1, 1));
        return f;
    }

    // ================================================================
    // PARTNERS
    // ================================================================
    private List<Partner> seedPartners() {
        List<Partner> list = new ArrayList<>();

        list.add(buildPartner("Công ty TNHH ABC Solutions", "CUSTOMER",
                "contact@abcsolutions.vn", "12 Nguyễn Huệ, Quận 1, TP.HCM"));
        list.add(buildPartner("Công ty CP XYZ Trading", "SUPPLIER",
                "info@xyztrading.vn", "45 Lê Lợi, Quận 3, TP.HCM"));
        list.add(buildPartner("Nguyễn Văn Bình", "CUSTOMER",
                "binhnguyen@gmail.com", "78 Trần Hưng Đạo, Quận 5, TP.HCM"));
        list.add(buildPartner("Công ty TNHH Thương Mại Phát Đạt", "SUPPLIER",
                "phatdat@company.vn", "23 Cách Mạng Tháng 8, Quận 10, TP.HCM"));
        list.add(buildPartner("Trần Thị Mai", "CUSTOMER",
                "maitran@gmail.com", "56 Võ Văn Tần, Quận 3, TP.HCM"));

        return partnerRepository.saveAll(list);
    }

    private Partner buildPartner(String name, String type, String email, String address) {
        Partner p = new Partner();
        p.setName(name);
        p.setType(type);
        p.setEmail(email);
        p.setAddress(address);
        p.setIsDeleted(false);
        return p;
    }

    // ================================================================
    // DEBTS
    // ================================================================
    private List<Debt> seedDebts(List<User> users, List<Category> categories, List<Partner> partners) {
        List<Debt> list = new ArrayList<>();

        // 1. Còn nợ - RECEIVABLE (đối tác nợ mình)
        list.add(buildDebt(date(2026, 6, 1), "RECEIVABLE", 15_000_000.0, 5_000_000.0, false,
                null, partners.get(2), categories.get(1), users.get(1),
                "Công nợ bán hàng tháng 6", "Khách hàng thanh toán trước 1/3 giá trị đơn hàng",
                date(2026, 6, 1), date(2026, 6, 5)));

        // 2. Đã trả xong - PAYABLE
        list.add(buildDebt(date(2026, 5, 10), "PAYABLE", 25_000_000.0, 25_000_000.0, true,
                date(2026, 6, 2), partners.get(1), categories.get(5), users.get(1),
                "Thanh toán nguyên vật liệu tháng 5", "Đã thanh toán đầy đủ cho nhà cung cấp",
                date(2026, 5, 10), date(2026, 6, 2)));

        // 3. Chưa trả - RECEIVABLE
        list.add(buildDebt(date(2026, 6, 15), "RECEIVABLE", 8_000_000.0, 0.0, false,
                null, partners.get(4), categories.get(1), users.get(2),
                "Công nợ dịch vụ tư vấn", "Khách hàng hẹn thanh toán vào cuối tháng",
                date(2026, 6, 15), date(2026, 6, 15)));

        // 4. Còn nợ 1 phần - PAYABLE
        list.add(buildDebt(date(2026, 6, 20), "PAYABLE", 12_500_000.0, 6_000_000.0, false,
                null, partners.get(3), categories.get(5), users.get(1),
                "Nợ tiền hàng nhập kho", "Đã ứng trước 6.000.000đ, phần còn lại thanh toán sau 30 ngày",
                date(2026, 6, 20), date(2026, 6, 25)));

        // 5. Đã trả xong - RECEIVABLE
        list.add(buildDebt(date(2026, 4, 5), "RECEIVABLE", 30_000_000.0, 30_000_000.0, true,
                date(2026, 5, 1), partners.get(0), categories.get(1), users.get(2),
                "Thanh lý hợp đồng dự án Q1/2026", "Khách hàng đã tất toán toàn bộ công nợ",
                date(2026, 4, 5), date(2026, 5, 1)));

        return debtRepository.saveAll(list);
    }

    private Debt buildDebt(Date debtDate, String debtType, Double totalAmount, Double paidAmount,
                           Boolean isPaid, Date paymentDate, Partner partner, Category category,
                           User user, String title, String note, Date createdAt, Date updatedAt) {
        Debt d = new Debt();
        d.setDebtDate(debtDate);
        d.setDebtType(debtType);
        d.setTotalAmount(totalAmount);
        d.setPaidAmount(paidAmount);
        d.setIsPaid(isPaid);
        d.setPaymentDate(paymentDate);
        d.setPartner(partner);
        d.setCategory(category);
        d.setUser(user);
        d.setTitle(title);
        d.setNote(note);
        d.setIsDeleted(false);
        d.setCreatedAt(createdAt);
        d.setUpdatedAt(updatedAt);
        return d;
    }

    // ================================================================
    // TRANSACTIONS (30 giao dịch sử dụng Enum status và warningLevel mới)
    // ================================================================
    private void seedTransactions(List<User> users, List<Category> categories, List<Fund> funds,
                                  List<Partner> partners, List<Debt> debts) {
        List<Transaction> list = new ArrayList<>();

        list.add(tx("GD202606001", funds.get(1), categories.get(1), partners.get(0), users.get(1),
                "INCOME", 45_000_000.0, "Thu tiền bán hàng đợt 1 tháng 6",
                "ACTIVE", date(2026, 6, 2), "Thanh toán hợp đồng dịch vụ",
                "Trần Thị Kế Toán", "HD-2026-001.pdf", false, "NORMAL", null));

        list.add(tx("GD202606002", funds.get(0), categories.get(3), null, users.get(2),
                "EXPENSE", 3_500_000.0, "Mua văn phòng phẩm quý 2",
                "ACTIVE", date(2026, 6, 3), "Bổ sung vật dụng văn phòng",
                "Lê Văn Thu Chi", "PN-2026-014.pdf", false, "NORMAL", null));

        list.add(tx("GD202606003", funds.get(1), categories.get(4), partners.get(1), users.get(1),
                "EXPENSE", 15_000_000.0, "Chi phí chạy quảng cáo Facebook/Google tháng 6",
                "ACTIVE", date(2026, 6, 4), "Chiến dịch marketing sản phẩm mới",
                "Trần Thị Kế Toán", "HD-2026-002.pdf", false, "NORMAL", null));

        list.add(tx("GD202606004", funds.get(2), categories.get(0), null, users.get(0),
                "EXPENSE", 60_000_000.0, "Chi trả lương nhân viên tháng 6",
                "ACTIVE", date(2026, 6, 5), "Bảng lương tháng 6/2026",
                "Nguyễn Văn Quản Trị", "BL-2026-06.xlsx", false, "NORMAL", null));

        list.add(tx("GD202606005", funds.get(1), categories.get(1), partners.get(2), users.get(1),
                "INCOME_DEBT", 5_000_000.0, "Thu một phần công nợ bán hàng tháng 6",
                "ACTIVE", date(2026, 6, 5), "Thanh toán công nợ theo phiếu D001",
                "Trần Thị Kế Toán", "PT-2026-011.pdf", false, "NORMAL", debts.get(0)));

        list.add(tx("GD202606006", funds.get(0), categories.get(5), partners.get(1), users.get(1),
                "EXPENSE_DEBT", 25_000_000.0, "Thanh toán nợ mua nguyên vật liệu tháng 5",
                "ACTIVE", date(2026, 6, 2), "Tất toán công nợ theo phiếu D002",
                "Trần Thị Kế Toán", "PC-2026-020.pdf", false, "NORMAL", debts.get(1)));

        list.add(tx("GD202606007", funds.get(0), categories.get(3), null, users.get(2),
                "EXPENSE", 2_200_000.0, "Chi phí điện nước văn phòng tháng 6",
                "ACTIVE", date(2026, 6, 6), "Hoá đơn điện nước định kỳ",
                "Lê Văn Thu Chi", "HD-DIEN-06.pdf", false, "NORMAL", null));

        list.add(tx("GD202606008", funds.get(1), categories.get(2), null, users.get(1),
                "INCOME", 6_000_000.0, "Thu nhập từ cho thuê thiết bị văn phòng",
                "ACTIVE", date(2026, 6, 7), "Hợp đồng cho thuê ngắn hạn",
                "Trần Thị Kế Toán", "HD-2026-003.pdf", false, "NORMAL", null));

        list.add(tx("GD202606009", funds.get(2), categories.get(5), partners.get(3), users.get(1),
                "EXPENSE", 6_500_000.0, "Ứng trước tiền hàng nhập kho",
                "ACTIVE", date(2026, 6, 20), "Ứng trước 1 phần công nợ D004",
                "Trần Thị Kế Toán", "PC-2026-021.pdf", false, "NORMAL", null));

        list.add(tx("GD202606010", funds.get(1), categories.get(1), partners.get(0), users.get(2),
                "INCOME", 30_000_000.0, "Thu tất toán hợp đồng dự án Q1/2026",
                "ACTIVE", date(2026, 5, 1), "Thu công nợ theo phiếu D005",
                "Lê Văn Thu Chi", "PT-2026-005.pdf", false, "NORMAL", debts.get(4)));

        list.add(tx("GD202606011", funds.get(0), categories.get(4), null, users.get(1),
                "EXPENSE", 4_800_000.0, "Chi phí in ấn tờ rơi, banner quảng cáo",
                "ACTIVE", date(2026, 6, 8), "Chuẩn bị sự kiện khai trương",
                "Trần Thị Kế Toán", "HD-2026-004.pdf", false, "NORMAL", null));

        list.add(tx("GD202606012", funds.get(1), categories.get(1), partners.get(4), users.get(1),
                "INCOME", 12_000_000.0, "Thu tiền dịch vụ tư vấn khách hàng",
                "ACTIVE", date(2026, 6, 15), "Chờ khách hàng chuyển khoản xác nhận",
                "Trần Thị Kế Toán", "HD-2026-005.pdf", true, "WARNING", null));

        list.add(tx("GD202606013", funds.get(0), categories.get(3), null, users.get(2),
                "EXPENSE", 1_800_000.0, "Chi phí sửa chữa máy tính văn phòng",
                "ACTIVE", date(2026, 6, 9), "Bảo trì thiết bị định kỳ",
                "Lê Văn Thu Chi", "PN-2026-015.pdf", false, "NORMAL", null));

        list.add(tx("GD202606014", funds.get(2), categories.get(5), partners.get(1), users.get(1),
                "EXPENSE", 18_000_000.0, "Nhập nguyên vật liệu sản xuất đợt 2",
                "ACTIVE", date(2026, 6, 10), "Bổ sung tồn kho nguyên liệu",
                "Trần Thị Kế Toán", "PN-2026-016.pdf", false, "NORMAL", null));

        list.add(tx("GD202606015", funds.get(1), categories.get(2), null, users.get(0),
                "INCOME", 3_200_000.0, "Thu lãi tiền gửi ngân hàng tháng 6",
                "ACTIVE", date(2026, 6, 30), "Lãi suất tiền gửi có kỳ hạn",
                "Nguyễn Văn Quản Trị", "SAO-KE-06.pdf", false, "NORMAL", null));

        list.add(tx("GD202606016", funds.get(0), categories.get(4), partners.get(2), users.get(1),
                "EXPENSE", 55_000_000.0, "Chi phí thuê mặt bằng văn phòng quý 3",
                "ACTIVE", date(2026, 6, 11), "Thanh toán tiền thuê 3 tháng",
                "Trần Thị Kế Toán", "HD-2026-006.pdf", true, "CRITICAL", null));

        list.add(tx("GD202606017", funds.get(1), categories.get(1), partners.get(3), users.get(2),
                "INCOME", 9_500_000.0, "Thu tiền bán hàng đợt 2 tháng 6",
                "ACTIVE", date(2026, 6, 12), "Đơn hàng số HD-778",
                "Lê Văn Thu Chi", "HD-2026-007.pdf", false, "NORMAL", null));

        list.add(tx("GD202606018", funds.get(0), categories.get(3), null, users.get(1),
                "EXPENSE", 950_000.0, "Chi phí tiếp khách công ty đối tác",
                "ACTIVE", date(2026, 6, 13), "Gặp gỡ đối tác ABC Solutions",
                "Trần Thị Kế Toán", "PN-2026-017.pdf", false, "NORMAL", null));

        list.add(tx("GD202606019", funds.get(2), categories.get(0), null, users.get(0),
                "EXPENSE", 7_000_000.0, "Chi thưởng hiệu suất nhân viên quý 2",
                "ACTIVE", date(2026, 6, 25), "Thưởng KPI quý 2/2026",
                "Nguyễn Văn Quản Trị", "QD-2026-002.pdf", false, "NORMAL", null));

        list.add(tx("GD202606020", funds.get(1), categories.get(1), partners.get(4), users.get(1),
                "EXPENSE", 5_000_000.0, "Hủy giao dịch đặt cọc do đối tác đổi ý",
                "CANCELLED", date(2026, 6, 28), "Chờ ký kết hợp đồng chính thức nhưng thất bại",
                "Trần Thị Kế Toán", "HD-2026-008.pdf", false, "NORMAL", null));

        // 10 Giao dịch bổ sung mới
        list.add(tx("GD202606021", funds.get(1), categories.get(1), partners.get(4), users.get(3),
                "INCOME_DEBT", 8_000_000.0, "Thu hồi nợ - Tất toán tiền nợ dịch vụ tư vấn",
                "ACTIVE", date(2026, 6, 30), "Tất toán công nợ theo phiếu D003",
                "Nguyen Minh Luan", "PT-2026-021.pdf", false, "NORMAL", debts.get(2)));

        list.add(tx("GD202606022", funds.get(2), categories.get(5), partners.get(3), users.get(1),
                "EXPENSE_DEBT", 6_000_000.0, "Thanh toán nợ - Trả nợ tiền hàng nhập kho đợt 2",
                "ACTIVE", date(2026, 6, 25), "Trả bớt công nợ D004",
                "Trần Thị Kế Toán", "PC-2026-025.pdf", false, "NORMAL", debts.get(3)));

        list.add(tx("GD202606023", funds.get(0), categories.get(3), null, users.get(2),
                "EXPENSE", 1_500_000.0, "Chi phí mua trà, cafe, nước uống văn phòng tháng 6",
                "ACTIVE", date(2026, 6, 26), "Tiền tạp phí văn phòng",
                "Lê Văn Thu Chi", "PN-2026-019.pdf", false, "NORMAL", null));

        list.add(tx("GD202606024", funds.get(1), categories.get(4), partners.get(0), users.get(1),
                "EXPENSE", 22_000_000.0, "Chi phí tổ chức Workshop giới thiệu giải pháp công nghệ",
                "ACTIVE", date(2026, 6, 27), "Sự kiện Marketing kết hợp ABC Solutions",
                "Trần Thị Kế Toán", "HD-2026-009.pdf", true, "WARNING", null));

        list.add(tx("GD202606025", funds.get(1), categories.get(1), partners.get(0), users.get(1),
                "INCOME", 55_000_000.0, "Thu tiền nghiệm thu dự án phần mềm giai đoạn 2",
                "ACTIVE", date(2026, 6, 29), "Nghiệm thu hợp đồng ABC-02",
                "Trần Thị Kế Toán", "PT-2026-022.pdf", false, "NORMAL", null));

        list.add(tx("GD202607001", funds.get(1), categories.get(1), partners.get(2), users.get(2),
                "INCOME", 14_500_000.0, "Doanh thu bán lô hàng linh kiện điện tử đầu tháng 7",
                "ACTIVE", date(2026, 7, 1), "Bán lẻ cho khách hàng Nguyễn Văn Bình",
                "Lê Văn Thu Chi", "HD-2026-010.pdf", false, "NORMAL", null));

        list.add(tx("GD202607002", funds.get(0), categories.get(3), null, users.get(2),
                "EXPENSE", 4_200_000.0, "Chi phí gửi xe và dọn dẹp vệ sinh tòa nhà tháng 7",
                "ACTIVE", date(2026, 7, 2), "Phí dịch vụ tòa nhà định kỳ",
                "Lê Văn Thu Chi", "HD-QLTN-07.pdf", false, "NORMAL", null));

        list.add(tx("GD202607003", funds.get(2), categories.get(5), partners.get(1), users.get(1),
                "EXPENSE", 35_000_000.0, "Nhập kho lô nguyên vật liệu nhựa chính tháng 7",
                "ACTIVE", date(2026, 7, 3), "Hợp đồng cung ứng vật liệu với XYZ Trading",
                "Trần Thị Kế Toán", "PN-2026-021.pdf", true, "CRITICAL", null));

        list.add(tx("GD202607004", funds.get(1), categories.get(2), null, users.get(0),
                "INCOME", 2_500_000.0, "Thu tiền thanh lý bàn ghế cũ văn phòng",
                "ACTIVE", date(2026, 7, 4), "Thanh lý tài sản cũ không sử dụng",
                "Nguyễn Văn Quản Trị", "BBTL-2026-01.pdf", false, "NORMAL", null));

        list.add(tx("GD202607005", funds.get(1), categories.get(4), null, users.get(1),
                "EXPENSE", 8_000_000.0, "Tạm ứng chi phí chạy Ads Google đầu tháng 7",
                "ACTIVE", date(2026, 7, 5), "Ngân sách Marketing chạy sản phẩm mới",
                "Trần Thị Kế Toán", "DN-2026-005.pdf", false, "NORMAL", null));

        transactionRepository.saveAll(list);
    }

    private Transaction tx(String code, Fund fund, Category category, Partner partner, User user,
                           String type, Double amount, String note, String status, Date transactionDate,
                           String reason, String accompaniedBy, String originalDocuments,
                           Boolean hasWarning, String warningLevel, Debt debt) {
        Transaction t = new Transaction();
        t.setFund(fund);
        t.setCategories(category);
        t.setPartner(partner);
        t.setUser(user);
        t.setParentId(null);
        t.setTransaction_code(code);
        t.setTransaction_date(transactionDate);
        t.setType(type);
        t.setAmount(amount);
        t.setAmountInWord(MoneyToWordsConverter.convert(amount.longValue()));
        t.setNote(note);
        t.setStatus(status);
        t.setCreated_at(transactionDate);
        t.setDatetime(transactionDate);
        t.setReason(reason);
        t.setAccompaniedBy(accompaniedBy);
        t.setOriginalDocuments(originalDocuments);
        t.setHasWarning(hasWarning);
        t.setWarningLevel(warningLevel);
        t.setDebt(debt);
        return t;
    }

    private Date date(int year, int month, int day) {
        return Date.from(LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}