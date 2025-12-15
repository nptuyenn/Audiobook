package com.example.audiobook_for_kids;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiobook_for_kids.adapter.AudiobookAdapter;
import com.example.audiobook_for_kids.model.Book;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvFeatured;
    private RecyclerView rvSuggestions;
    private RecyclerView rvQuickPicks;
    private RecyclerView rvFamousAuthors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvFeatured = findViewById(R.id.rv_featured);
        rvSuggestions = findViewById(R.id.rv_suggestions);
        rvQuickPicks = findViewById(R.id.rv_quick_picks);
        rvFamousAuthors = findViewById(R.id.rv_famous_authors);

        setupFeaturedRecycler();

        setupSuggestionsRecycler();

        setupQuickPicksRecycler();

        setupFamousAuthorsRecycler();

        setupBottomNavigation();

        setupTopicClickListeners();

        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            // Hiệu ứng: LoginActivity trượt lên từ dưới, MainActivity giữ nguyên (smooth version)
            overridePendingTransition(R.anim.slide_in_up, R.anim.no_animation);
        });

        LinearLayout search_bar = findViewById(R.id.search_bar);
        search_bar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void setupFeaturedRecycler() {
        // Cấu hình LayoutManager (cuộn ngang)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvFeatured.setLayoutManager(layoutManager);

        List<Book> featuredBooks = getMockFeaturedBooks();

        AudiobookAdapter featuredAdapter = new AudiobookAdapter(this, featuredBooks, book -> {
            // Xử lý khi click vào sách
            openBookDetail(book);
        });
        rvFeatured.setAdapter(featuredAdapter);
    }

    private void setupSuggestionsRecycler() {
        // Cấu hình LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvSuggestions.setLayoutManager(layoutManager);

        // ===== DỮ LIỆU GIẢ - MOCK DATA =====
        List<Book> suggestionBooks = getMockSuggestionBooks();

        AudiobookAdapter suggestionsAdapter = new AudiobookAdapter(this, suggestionBooks, book -> {
            // Xử lý khi click vào sách
            openBookDetail(book);
        });
        rvSuggestions.setAdapter(suggestionsAdapter);
    }


    private List<Book> getMockFeaturedBooks() {
        List<Book> books = new ArrayList<>();

        books.add(new Book(
            "mock_1",
            "Cô Bé Quàng Khăn Đỏ",
            "Grimm Brothers",
            "https://picsum.photos/seed/red-riding-hood/300/400",
            "Câu chuyện cổ tích nổi tiếng về cô bé áo choàng đỏ và chú sói xấu xa",
            "co_tich"
        ));

        books.add(new Book(
            "mock_2",
            "Ba Chú Heo Con",
            "Joseph Jacobs",
            "https://picsum.photos/seed/three-pigs/300/400",
            "Ba chú heo xây nhà và cuộc chạm trán với chú sói hung dữ",
            "co_tich"
        ));

        books.add(new Book(
            "mock_3",
            "Nàng Bạch Tuyết",
            "Grimm Brothers",
            "https://picsum.photos/seed/snow-white/300/400",
            "Câu chuyện về nàng công chúa đẹp nhất và bảy chú lùn",
            "co_tich"
        ));

        books.add(new Book(
            "mock_4",
            "Cô Bé Lọ Lem",
            "Charles Perrault",
            "https://picsum.photos/seed/cinderella/300/400",
            "Câu chuyện cổ tích lãng mạn về cô bé lọ lem và hoàng tử",
            "co_tich"
        ));

        books.add(new Book(
            "mock_5",
            "Pinocchio",
            "Carlo Collodi",
            "https://picsum.photos/seed/pinocchio/300/400",
            "Chú bé người gỗ và hành trình trở thành người thật",
            "nuoc_ngoai"
        ));

        return books;
    }


    private List<Book> getMockSuggestionBooks() {
        List<Book> books = new ArrayList<>();


        books.add(new Book(
            "mock_6",
            "Thỏ và Rùa",
            "Aesop",
            "https://picsum.photos/seed/tortoise-hare/300/400",
            "Câu chuyện ngụ ngôn về cuộc đua của thỏ và rùa",
            "ngu_ngon"
        ));

        books.add(new Book(
            "mock_7",
            "Kiến và Châu Chấu",
            "Aesop",
            "https://picsum.photos/seed/ant-grasshopper/300/400",
            "Bài học về sự cần cù và sự lười biếng",
            "ngu_ngon"
        ));

        books.add(new Book(
            "mock_8",
            "Alice Ở Xứ Sở Thần Tiên",
            "Lewis Carroll",
            "https://picsum.photos/seed/alice-wonderland/300/400",
            "Cuộc phiêu lưu kỳ diệu của Alice trong xứ sở thần tiên",
            "phieu_luu"
        ));

        books.add(new Book(
            "mock_9",
            "Peter Pan",
            "J.M. Barrie",
            "https://picsum.photos/seed/peter-pan/300/400",
            "Cậu bé không bao giờ lớn và vùng đất Neverland",
            "phieu_luu"
        ));

        books.add(new Book(
            "mock_10",
            "Bé Học Làm Người Tốt",
            "Nhiều tác giả",
            "https://picsum.photos/seed/good-person/300/400",
            "Những câu chuyện giáo dục đạo đức cho trẻ em",
            "giao_duc"
        ));

        return books;
    }

    private void setupQuickPicksRecycler() {
        // Cấu hình LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvQuickPicks.setLayoutManager(layoutManager);

        List<Book> quickPicksBooks = getMockQuickPicksBooks();

        AudiobookAdapter quickPicksAdapter = new AudiobookAdapter(this, quickPicksBooks, book -> {
            openBookDetail(book);
        });
        rvQuickPicks.setAdapter(quickPicksAdapter);
    }


    private void setupFamousAuthorsRecycler() {
        // Cấu hình LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvFamousAuthors.setLayoutManager(layoutManager);

        List<Book> famousAuthorsBooks = getMockFamousAuthorsBooks();

        AudiobookAdapter famousAuthorsAdapter = new AudiobookAdapter(this, famousAuthorsBooks, book -> {
            openBookDetail(book);
        });
        rvFamousAuthors.setAdapter(famousAuthorsAdapter);
    }

    //tạo dữ liệu giả
    private List<Book> getMockQuickPicksBooks() {
        List<Book> books = new ArrayList<>();

        // NOTE: Sử dụng picsum.photos cho ảnh placeholder


        books.add(new Book(
            "mock_11",
            "Harry Potter và Hòn Đá Phù Thủy",
            "J.K. Rowling",
            "https://picsum.photos/seed/harry-potter/300/400",
            "Cậu bé phù thủy và cuộc phiêu lưu kỳ diệu",
            "phieu_luu"
        ));

        books.add(new Book(
            "mock_12",
            "Dế Mèn Phiêu Lưu Ký",
            "Tô Hoài",
            "https://picsum.photos/seed/de-men/300/400",
            "Cuộc phiêu lưu của chú dế Mèn qua nhiều vùng đất",
            "phieu_luu"
        ));

        books.add(new Book(
            "mock_13",
            "Cuốn Theo Chiều Gió",
            "Margaret Mitchell",
            "https://picsum.photos/seed/gone-with-wind/300/400",
            "Câu chuyện tình yêu trong thời chiến tranh",
            "nuoc_ngoai"
        ));

        books.add(new Book(
            "mock_14",
            "Những Cuộc Phiêu Lưu Của Tom Sawyer",
            "Mark Twain",
            "https://picsum.photos/seed/tom-sawyer/300/400",
            "Những trò nghịch ngợm và phiêu lưu của Tom",
            "phieu_luu"
        ));

        books.add(new Book(
            "mock_15",
            "Sư Tử, Phù Thủy Và Cái Tủ Áo",
            "C.S. Lewis",
            "https://picsum.photos/seed/narnia/300/400",
            "Hành trình kỳ diệu đến vương quốc Narnia",
            "nuoc_ngoai"
        ));

        return books;
    }


    private List<Book> getMockFamousAuthorsBooks() {
        List<Book> books = new ArrayList<>();

        // NOTE: Sách của các tác giả nổi tiếng
        // TODO: Thay bằng dữ liệu thật từ Firebase

        books.add(new Book(
            "mock_16",
            "Chú Vịt Con Xấu Xí",
            "Hans Christian Andersen",
            "https://picsum.photos/seed/ugly-duckling/300/400",
            "Câu chuyện về chú vịt con trở thành thiên nga",
            "co_tich"
        ));

        books.add(new Book(
            "mock_17",
            "Nàng Tiên Cá",
            "Hans Christian Andersen",
            "https://picsum.photos/seed/little-mermaid/300/400",
            "Nàng tiên cá yêu hoàng tử đất liền",
            "co_tich"
        ));

        books.add(new Book(
            "mock_18",
            "Rô-bin-sơn Ngoài Hoang Đảo",
            "Daniel Defoe",
            "https://picsum.photos/seed/robinson-crusoe/300/400",
            "Cuộc sống của người đàn ông trên hoang đảo",
            "phieu_luu"
        ));

        books.add(new Book(
            "mock_19",
            "Huckleberry Finn",
            "Mark Twain",
            "https://picsum.photos/seed/huck-finn/300/400",
            "Cuộc phiêu lưu trên dòng sông Mississippi",
            "phieu_luu"
        ));

        books.add(new Book(
            "mock_20",
            "Anh Em Nhà Grimm",
            "Grimm Brothers",
            "https://picsum.photos/seed/grimm-tales/300/400",
            "Tuyển tập truyện cổ tích kinh điển",
            "co_tich"
        ));

        return books;
    }

    // TODO: Sau này thêm các method  để load dữ liệu

    private void openBookDetail(Book book) {
        try {
            Intent intent = new Intent(this, AudiobookDetailActivity.class);
            intent.putExtra("book_id", book.getId());
            intent.putExtra("book_title", book.getTitle());
            intent.putExtra("book_author", book.getAuthor());
            intent.putExtra("book_cover", book.getCoverUrl());
            intent.putExtra("book_description", book.getDescription());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở chi tiết sách", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_home) {
                // Đã ở trang chủ
                return true;
            } else if (itemId == R.id.nav_ai) {
                Intent intent = new Intent(MainActivity.this, AIStoryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_library) {
                Intent intent = new Intent(MainActivity.this, LibraryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_account) {
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    private void setupTopicClickListeners() {
        findViewById(R.id.topic_co_tich).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_CO_TICH, "Cổ tích"));

        findViewById(R.id.topic_nuoc_ngoai).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_NUOC_NGOAI, "Nước ngoài"));

        findViewById(R.id.topic_ngu_ngon).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_NGU_NGON, "Ngụ ngôn"));

        findViewById(R.id.topic_giao_duc).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_GIAO_DUC, "Giáo dục"));

        findViewById(R.id.topic_phieu_luu).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_PHIEU_LUU, "Phiêu lưu"));
    }

    private void openTopicActivity(String topicType, String topicTitle) {
        try {
            Intent intent = new Intent(this, TopicActivity.class);
            intent.putExtra(TopicActivity.EXTRA_TOPIC_TYPE, topicType);
            intent.putExtra(TopicActivity.EXTRA_TOPIC_TITLE, topicTitle);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở chủ đề", Toast.LENGTH_SHORT).show();
        }
    }
}