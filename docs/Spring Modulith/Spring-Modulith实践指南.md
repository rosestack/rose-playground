# Spring Modulith 实践指南

## 📖 目录

1. [概述](#概述)
2. [核心概念](#核心概念)
3. [快速开始](#快速开始)
4. [项目结构](#项目结构)
5. [模块设计](#模块设计)
6. [事件驱动](#事件驱动)
7. [测试策略](#测试策略)
8. [文档生成](#文档生成)
9. [最佳实践](#最佳实践)
10. [实战案例](#实战案例)

## 🎯 概述

### 什么是 Spring Modulith

Spring Modulith 是 Spring 官方项目，用于构建**模块化单体应用**（Modular Monolith）。它提供了：

- 📦 **基于包结构的模块化**
- 🔍 **模块验证和测试工具**
- 📚 **自动文档生成**
- 📊 **可观测性支持**
- 🔄 **事件驱动的模块间通信**

### 为什么选择 Spring Modulith

#### ✅ 优势
- **学习成本低**: 基于熟悉的Spring Boot和包结构
- **渐进式采用**: 可以在现有项目中逐步引入
- **工具支持好**: Spring官方维护，工具链完善
- **测试友好**: 提供模块级别的集成测试
- **文档自动化**: 自动生成模块关系图和文档

#### 🎯 适用场景
- 中小型团队的单体应用
- 需要模块化但不想拆分微服务
- 希望保持部署简单性的项目
- 从传统分层架构向模块化演进

### Spring Modulith vs DDD 对比

| 特性 | Spring Modulith | DDD 四层架构 |
|------|----------------|-------------|
| **复杂度** | 简单 | 复杂 |
| **学习曲线** | 平缓 | 陡峭 |
| **实施成本** | 低 | 高 |
| **工具支持** | 优秀 | 一般 |
| **理论完整性** | 实用 | 完整 |
| **适用规模** | 中小型 | 大型 |

## 🔧 核心概念

### 1. 应用模块 (Application Module)

Spring Modulith 中的模块是基于 **Java 包结构** 定义的逻辑边界：

```
com.example.library/
├── catalog/           # 图书目录模块
├── borrow/           # 借阅模块  
├── patron/           # 读者模块
└── shared/           # 共享模块
```

### 2. 模块类型

#### 🔒 **封闭模块** (Closed Module)
```java
// 只有 API 包对外可见
com.example.library.catalog/
├── api/              # 对外API (public)
├── internal/         # 内部实现 (package-private)
└── CatalogModule.java
```

#### 🔓 **开放模块** (Open Module)  
```java
// 所有包都对外可见
com.example.library.shared/
├── events/
├── config/
└── utils/
```

### 3. 模块间通信

#### 直接调用
```java
@Component
public class BorrowService {
    
    private final CatalogApi catalogApi; // 直接依赖其他模块API
    
    public void borrowBook(String isbn) {
        Book book = catalogApi.findByIsbn(isbn);
        // 借阅逻辑
    }
}
```

#### 事件驱动
```java
@Component
public class BorrowService {
    
    private final ApplicationEventPublisher events;
    
    public void borrowBook(String isbn) {
        // 借阅逻辑
        events.publishEvent(new BookBorrowedEvent(isbn));
    }
}
```

## 🚀 快速开始

### 1. 项目依赖

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Modulith -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-core</artifactId>
    </dependency>
    
    <!-- 测试支持 -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- 文档生成 -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-docs</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2. 基础配置

```java
@SpringBootApplication
@EnableModulithRepositories // 启用模块化仓储
public class LibraryApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
}
```

### 3. 模块验证测试

```java
@ModulithTest
class ModularityTests {
    
    @Test
    void verifyModularity() {
        ApplicationModules.of(LibraryApplication.class)
            .verify(); // 验证模块结构
    }
    
    @Test
    void writeDocumentation() throws IOException {
        ApplicationModules modules = ApplicationModules.of(LibraryApplication.class);
        
        new Documenter(modules)
            .writeModulesAsPlantUml()    // 生成PlantUML图
            .writeIndividualModulesAsPlantUml(); // 生成各模块详细图
    }
}
```

## 📁 项目结构

### 标准目录结构

```
src/main/java/com/example/library/
├── LibraryApplication.java          # 主应用类
├── catalog/                         # 图书目录模块
│   ├── api/                        # 对外API
│   │   ├── CatalogApi.java
│   │   └── dto/
│   ├── internal/                   # 内部实现
│   │   ├── CatalogService.java
│   │   ├── BookRepository.java
│   │   └── Book.java
│   └── CatalogConfiguration.java   # 模块配置
├── borrow/                         # 借阅模块
│   ├── api/
│   │   ├── BorrowApi.java
│   │   └── dto/
│   ├── internal/
│   │   ├── BorrowService.java
│   │   ├── HoldRepository.java
│   │   └── Hold.java
│   └── BorrowConfiguration.java
├── patron/                         # 读者模块
│   ├── api/
│   ├── internal/
│   └── PatronConfiguration.java
└── shared/                         # 共享模块
    ├── events/                     # 领域事件
    ├── config/                     # 共享配置
    └── security/                   # 安全配置
```

### 模块配置示例

```java
// catalog/CatalogConfiguration.java
@Configuration
@ComponentScan
@EnableJpaRepositories
public class CatalogConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public CatalogApi catalogApi(CatalogService catalogService) {
        return new CatalogApiImpl(catalogService);
    }
}
```

## 🎨 模块设计

### 1. API 设计模式

#### 接口定义
```java
// catalog/api/CatalogApi.java
public interface CatalogApi {
    
    /**
     * 根据ISBN查找图书
     */
    Optional<BookDto> findByIsbn(String isbn);
    
    /**
     * 添加新书到目录
     */
    BookDto addBook(CreateBookRequest request);
    
    /**
     * 检查图书是否可借
     */
    boolean isBookAvailable(String isbn);
}
```

#### DTO 定义
```java
// catalog/api/dto/BookDto.java
public record BookDto(
    Long id,
    String title,
    String isbn,
    String author,
    BookStatus status
) {}

public record CreateBookRequest(
    String title,
    String isbn,
    String author,
    String catalogNumber
) {}
```

#### 实现类
```java
// catalog/internal/CatalogApiImpl.java
@Component
class CatalogApiImpl implements CatalogApi {
    
    private final CatalogService catalogService;
    
    @Override
    public Optional<BookDto> findByIsbn(String isbn) {
        return catalogService.findByIsbn(isbn)
            .map(this::toDto);
    }
    
    @Override
    public BookDto addBook(CreateBookRequest request) {
        Book book = catalogService.createBook(request);
        return toDto(book);
    }
    
    private BookDto toDto(Book book) {
        return new BookDto(
            book.getId(),
            book.getTitle(),
            book.getIsbn(),
            book.getAuthor(),
            book.getStatus()
        );
    }
}
```

### 2. 内部服务设计

```java
// catalog/internal/CatalogService.java
@Service
@Transactional
class CatalogService {
    
    private final BookRepository bookRepository;
    private final ApplicationEventPublisher events;
    
    public Book createBook(CreateBookRequest request) {
        // 验证ISBN唯一性
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateIsbnException(request.isbn());
        }
        
        // 创建图书
        Book book = Book.builder()
            .title(request.title())
            .isbn(request.isbn())
            .author(request.author())
            .catalogNumber(request.catalogNumber())
            .status(BookStatus.AVAILABLE)
            .build();
            
        Book savedBook = bookRepository.save(book);
        
        // 发布事件
        events.publishEvent(new BookAddedEvent(savedBook.getId(), savedBook.getIsbn()));
        
        return savedBook;
    }
    
    public Optional<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
    
    public void markAsUnavailable(String isbn) {
        bookRepository.findByIsbn(isbn)
            .ifPresent(book -> {
                book.setStatus(BookStatus.BORROWED);
                bookRepository.save(book);
            });
    }
}
```

### 3. 实体设计

```java
// catalog/internal/Book.java
@Entity
@Table(name = "books")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(unique = true, nullable = false)
    private String isbn;
    
    @Column(nullable = false)
    private String author;
    
    @Column(name = "catalog_number", unique = true)
    private String catalogNumber;
    
    @Enumerated(EnumType.STRING)
    private BookStatus status;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

enum BookStatus {
    AVAILABLE, BORROWED, RESERVED, MAINTENANCE
}
```

## 🔄 事件驱动

### 1. 事件定义

```java
// shared/events/BookAddedEvent.java
public record BookAddedEvent(
    Long bookId,
    String isbn,
    Instant occurredAt
) {
    public BookAddedEvent(Long bookId, String isbn) {
        this(bookId, isbn, Instant.now());
    }
}

// shared/events/BookBorrowedEvent.java
public record BookBorrowedEvent(
    String isbn,
    Long patronId,
    LocalDate dueDate,
    Instant occurredAt
) {
    public BookBorrowedEvent(String isbn, Long patronId, LocalDate dueDate) {
        this(isbn, patronId, dueDate, Instant.now());
    }
}
```

### 2. 事件发布

```java
// borrow/internal/BorrowService.java
@Service
@Transactional
class BorrowService {

    private final HoldRepository holdRepository;
    private final CatalogApi catalogApi;
    private final ApplicationEventPublisher events;

    public void borrowBook(String isbn, Long patronId) {
        // 检查图书是否可借
        BookDto book = catalogApi.findByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        if (!catalogApi.isBookAvailable(isbn)) {
            throw new BookNotAvailableException(isbn);
        }

        // 创建借阅记录
        Hold hold = Hold.builder()
            .isbn(isbn)
            .patronId(patronId)
            .borrowedAt(LocalDateTime.now())
            .dueDate(LocalDate.now().plusWeeks(2))
            .status(HoldStatus.ACTIVE)
            .build();

        holdRepository.save(hold);

        // 发布借阅事件
        events.publishEvent(new BookBorrowedEvent(isbn, patronId, hold.getDueDate()));
    }
}
```

### 3. 事件监听

```java
// catalog/internal/CatalogEventHandler.java
@Component
@Transactional
class CatalogEventHandler {

    private final CatalogService catalogService;

    @EventListener
    public void handleBookBorrowed(BookBorrowedEvent event) {
        // 更新图书状态为已借出
        catalogService.markAsUnavailable(event.isbn());
    }

    @EventListener
    public void handleBookReturned(BookReturnedEvent event) {
        // 更新图书状态为可借
        catalogService.markAsAvailable(event.isbn());
    }
}

// patron/internal/PatronEventHandler.java
@Component
class PatronEventHandler {

    private final NotificationService notificationService;

    @EventListener
    @Async
    public void handleBookBorrowed(BookBorrowedEvent event) {
        // 发送借阅确认通知
        notificationService.sendBorrowConfirmation(
            event.patronId(),
            event.isbn(),
            event.dueDate()
        );
    }
}
```

### 4. 异步事件处理

```java
// shared/config/AsyncConfiguration.java
@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("library-async-");
        executor.initialize();
        return executor;
    }
}
```

## 🧪 测试策略

### 1. 模块验证测试

```java
@ModulithTest
class LibraryModularityTests {

    @Test
    void verifyModularity() {
        ApplicationModules modules = ApplicationModules.of(LibraryApplication.class);

        // 验证模块结构
        modules.verify();

        // 打印模块信息
        modules.forEach(System.out::println);
    }

    @Test
    void verifyNoCyclicDependencies() {
        ApplicationModules.of(LibraryApplication.class)
            .verify()
            .assertNoCycles(); // 验证无循环依赖
    }

    @Test
    void catalogModuleShouldNotDependOnBorrow() {
        ApplicationModules modules = ApplicationModules.of(LibraryApplication.class);

        ApplicationModule catalog = modules.getModuleByName("catalog")
            .orElseThrow();
        ApplicationModule borrow = modules.getModuleByName("borrow")
            .orElseThrow();

        // 验证目录模块不依赖借阅模块
        assertThat(catalog.getDependencies(modules))
            .doesNotContain(borrow);
    }
}
```

### 2. 模块集成测试

```java
@ModulithTest(mode = BootstrapMode.DIRECT_DEPENDENCIES)
class CatalogModuleIntegrationTests {

    @Autowired
    CatalogApi catalogApi;

    @Test
    void shouldAddBookToCatalog() {
        // Given
        CreateBookRequest request = new CreateBookRequest(
            "Spring in Action",
            "978-1617294945",
            "Craig Walls",
            "CAT001"
        );

        // When
        BookDto book = catalogApi.addBook(request);

        // Then
        assertThat(book.title()).isEqualTo("Spring in Action");
        assertThat(book.isbn()).isEqualTo("978-1617294945");
    }

    @Test
    void shouldFindBookByIsbn() {
        // Given
        String isbn = "978-1617294945";
        catalogApi.addBook(new CreateBookRequest(
            "Spring in Action", isbn, "Craig Walls", "CAT001"
        ));

        // When
        Optional<BookDto> book = catalogApi.findByIsbn(isbn);

        // Then
        assertThat(book).isPresent();
        assertThat(book.get().isbn()).isEqualTo(isbn);
    }
}
```

### 3. 事件测试

```java
@ModulithTest
class EventIntegrationTests {

    @Autowired
    BorrowApi borrowApi;

    @Autowired
    CatalogApi catalogApi;

    @Test
    void borrowingBookShouldUpdateCatalog() {
        // Given
        String isbn = "978-1617294945";
        catalogApi.addBook(new CreateBookRequest(
            "Spring in Action", isbn, "Craig Walls", "CAT001"
        ));

        // When
        borrowApi.borrowBook(isbn, 1L);

        // Then
        assertThat(catalogApi.isBookAvailable(isbn)).isFalse();
    }

    @Test
    void shouldPublishEventWhenBookBorrowed() {
        // Given
        String isbn = "978-1617294945";
        catalogApi.addBook(new CreateBookRequest(
            "Spring in Action", isbn, "Craig Walls", "CAT001"
        ));

        // When & Then
        assertThatEvents()
            .matching(BookBorrowedEvent.class, event -> event.isbn().equals(isbn))
            .arePublishedBy(() -> borrowApi.borrowBook(isbn, 1L));
    }
}
```

### 4. 场景测试

```java
@ModulithTest
@Transactional
class LibraryScenarioTests {

    @Autowired
    CatalogApi catalogApi;

    @Autowired
    BorrowApi borrowApi;

    @Autowired
    PatronApi patronApi;

    @Test
    void completeBookBorrowingScenario() {
        // 1. 添加图书到目录
        String isbn = "978-1617294945";
        BookDto book = catalogApi.addBook(new CreateBookRequest(
            "Spring in Action", isbn, "Craig Walls", "CAT001"
        ));

        // 2. 注册读者
        PatronDto patron = patronApi.registerPatron(new RegisterPatronRequest(
            "John Doe", "john@example.com"
        ));

        // 3. 借阅图书
        borrowApi.borrowBook(isbn, patron.id());

        // 4. 验证状态
        assertThat(catalogApi.isBookAvailable(isbn)).isFalse();
        assertThat(borrowApi.getActiveHolds(patron.id())).hasSize(1);

        // 5. 归还图书
        borrowApi.returnBook(isbn, patron.id());

        // 6. 验证最终状态
        assertThat(catalogApi.isBookAvailable(isbn)).isTrue();
        assertThat(borrowApi.getActiveHolds(patron.id())).isEmpty();
    }
}
```

## 📚 文档生成

### 1. 自动文档生成

```java
@Test
void writeDocumentation() throws IOException {
    ApplicationModules modules = ApplicationModules.of(LibraryApplication.class);

    new Documenter(modules)
        // 生成模块关系图
        .writeModulesAsPlantUml()
        // 生成各模块详细图
        .writeIndividualModulesAsPlantUml()
        // 生成模块画布
        .writeModuleCanvases();
}
```

### 2. 自定义文档

```java
@Test
void writeCustomDocumentation() throws IOException {
    ApplicationModules modules = ApplicationModules.of(LibraryApplication.class);

    Documenter documenter = new Documenter(modules);

    // 自定义样式
    documenter.writeModulesAsPlantUml(DiagramOptions.defaults()
        .withStyle(ComponentStyle.UML)
        .withColorSelector(module ->
            module.getName().equals("catalog") ? "#lightblue" : "#lightgreen"
        )
    );

    // 生成特定模块的详细文档
    ApplicationModule catalogModule = modules.getModuleByName("catalog")
        .orElseThrow();

    documenter.writeModuleAsPlantUml(catalogModule, "catalog-detail");
}
```

### 3. 集成到构建过程

```xml
<!-- Maven 插件配置 -->
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>generate-docs</id>
            <phase>test</phase>
            <goals>
                <goal>test</goal>
            </goals>
            <configuration>
                <includes>
                    <include>**/*DocumentationTests.java</include>
                </includes>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## 💡 最佳实践

### 1. 模块设计原则

#### 单一职责原则
```java
// ✅ 好的设计 - 职责单一
catalog/     # 只负责图书目录管理
borrow/      # 只负责借阅管理
patron/      # 只负责读者管理

// ❌ 不好的设计 - 职责混乱
library/     # 包含所有功能
```

#### 最小化模块间依赖
```java
// ✅ 好的设计 - 通过事件解耦
@EventListener
public void handleBookBorrowed(BookBorrowedEvent event) {
    catalogService.markAsUnavailable(event.isbn());
}

// ❌ 不好的设计 - 直接依赖
public void borrowBook(String isbn) {
    borrowService.createHold(isbn);
    catalogService.markAsUnavailable(isbn); // 直接调用其他模块
}
```

### 2. API 设计原则

#### 稳定的接口
```java
// ✅ 稳定的API接口
public interface CatalogApi {
    Optional<BookDto> findByIsbn(String isbn);
    BookDto addBook(CreateBookRequest request);
    boolean isBookAvailable(String isbn);
}

// ❌ 暴露内部实现
public interface CatalogApi {
    Book getBookEntity(String isbn); // 暴露内部实体
    void saveBook(Book book);        // 暴露内部操作
}
```

#### 版本化支持
```java
// API版本化
public interface CatalogApiV1 {
    BookDto findByIsbn(String isbn);
}

public interface CatalogApiV2 extends CatalogApiV1 {
    List<BookDto> findByAuthor(String author);
}
```

### 3. 事件设计原则

#### 事件命名
```java
// ✅ 过去时态，表示已发生的事实
public record BookBorrowedEvent(String isbn, Long patronId) {}
public record BookReturnedEvent(String isbn, Long patronId) {}

// ❌ 现在时态或命令式
public record BorrowBookEvent(String isbn, Long patronId) {}
public record ReturnBookCommand(String isbn, Long patronId) {}
```

#### 事件内容
```java
// ✅ 包含足够信息，避免回查
public record BookBorrowedEvent(
    String isbn,
    Long patronId,
    String bookTitle,
    LocalDate dueDate,
    Instant occurredAt
) {}

// ❌ 信息不足，需要回查数据库
public record BookBorrowedEvent(Long holdId) {}
```

### 4. 测试策略

#### 分层测试
```java
// 单元测试 - 测试单个组件
@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {
    @Mock BookRepository bookRepository;
    @InjectMocks CatalogService catalogService;
}

// 模块测试 - 测试模块内集成
@ModulithTest(mode = BootstrapMode.DIRECT_DEPENDENCIES)
class CatalogModuleTest {}

// 应用测试 - 测试跨模块场景
@ModulithTest
class LibraryApplicationTest {}
```

### 5. 性能优化

#### 异步事件处理
```java
@EventListener
@Async
public void handleBookBorrowed(BookBorrowedEvent event) {
    // 异步处理，不阻塞主流程
    notificationService.sendBorrowConfirmation(event);
}
```

#### 事件批处理
```java
@EventListener
@Async
public void handleBookEvents(List<BookEvent> events) {
    // 批量处理事件
    events.forEach(this::processEvent);
}
```

## 🎯 实战案例

### 案例：图书馆管理系统

#### 业务需求
- 图书目录管理（添加、查询、更新图书信息）
- 读者管理（注册、查询读者信息）
- 借阅管理（借书、还书、续借）
- 通知服务（借阅提醒、逾期通知）

#### 模块划分

```
com.example.library/
├── catalog/          # 图书目录模块
├── patron/           # 读者管理模块
├── borrow/           # 借阅管理模块
├── notification/     # 通知服务模块
└── shared/           # 共享模块
```

#### 完整实现示例

##### 1. 图书目录模块

```java
// catalog/api/CatalogApi.java
public interface CatalogApi {
    BookDto addBook(CreateBookRequest request);
    Optional<BookDto> findByIsbn(String isbn);
    List<BookDto> searchBooks(BookSearchCriteria criteria);
    boolean isBookAvailable(String isbn);
    void updateBookStatus(String isbn, BookStatus status);
}

// catalog/internal/CatalogService.java
@Service
@Transactional
class CatalogService {

    private final BookRepository bookRepository;
    private final ApplicationEventPublisher events;

    public BookDto addBook(CreateBookRequest request) {
        validateBookRequest(request);

        Book book = Book.builder()
            .title(request.title())
            .isbn(request.isbn())
            .author(request.author())
            .publisher(request.publisher())
            .publishedYear(request.publishedYear())
            .category(request.category())
            .status(BookStatus.AVAILABLE)
            .build();

        Book savedBook = bookRepository.save(book);

        events.publishEvent(new BookAddedEvent(
            savedBook.getId(),
            savedBook.getIsbn(),
            savedBook.getTitle()
        ));

        return toDto(savedBook);
    }

    public List<BookDto> searchBooks(BookSearchCriteria criteria) {
        Specification<Book> spec = BookSpecifications.withCriteria(criteria);
        return bookRepository.findAll(spec)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    private void validateBookRequest(CreateBookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateIsbnException(request.isbn());
        }
    }

    private BookDto toDto(Book book) {
        return new BookDto(
            book.getId(),
            book.getTitle(),
            book.getIsbn(),
            book.getAuthor(),
            book.getPublisher(),
            book.getPublishedYear(),
            book.getCategory(),
            book.getStatus()
        );
    }
}
```

##### 2. 借阅管理模块

```java
// borrow/api/BorrowApi.java
public interface BorrowApi {
    HoldDto borrowBook(String isbn, Long patronId);
    void returnBook(String isbn, Long patronId);
    HoldDto renewBook(String isbn, Long patronId);
    List<HoldDto> getActiveHolds(Long patronId);
    List<HoldDto> getOverdueHolds();
}

// borrow/internal/BorrowService.java
@Service
@Transactional
class BorrowService {

    private final HoldRepository holdRepository;
    private final CatalogApi catalogApi;
    private final PatronApi patronApi;
    private final ApplicationEventPublisher events;

    public HoldDto borrowBook(String isbn, Long patronId) {
        // 验证图书可借性
        validateBookAvailability(isbn);

        // 验证读者状态
        validatePatronStatus(patronId);

        // 检查借阅限制
        validateBorrowingLimits(patronId);

        // 创建借阅记录
        Hold hold = Hold.builder()
            .isbn(isbn)
            .patronId(patronId)
            .borrowedAt(LocalDateTime.now())
            .dueDate(calculateDueDate())
            .status(HoldStatus.ACTIVE)
            .renewalCount(0)
            .build();

        Hold savedHold = holdRepository.save(hold);

        // 更新图书状态
        catalogApi.updateBookStatus(isbn, BookStatus.BORROWED);

        // 发布借阅事件
        events.publishEvent(new BookBorrowedEvent(
            isbn, patronId, savedHold.getDueDate()
        ));

        return toDto(savedHold);
    }

    public void returnBook(String isbn, Long patronId) {
        Hold hold = findActiveHold(isbn, patronId);

        hold.setReturnedAt(LocalDateTime.now());
        hold.setStatus(HoldStatus.RETURNED);

        holdRepository.save(hold);

        // 更新图书状态
        catalogApi.updateBookStatus(isbn, BookStatus.AVAILABLE);

        // 发布归还事件
        events.publishEvent(new BookReturnedEvent(isbn, patronId));
    }

    private void validateBookAvailability(String isbn) {
        if (!catalogApi.isBookAvailable(isbn)) {
            throw new BookNotAvailableException(isbn);
        }
    }

    private void validatePatronStatus(Long patronId) {
        PatronDto patron = patronApi.findById(patronId)
            .orElseThrow(() -> new PatronNotFoundException(patronId));

        if (patron.status() != PatronStatus.ACTIVE) {
            throw new PatronNotActiveException(patronId);
        }
    }

    private void validateBorrowingLimits(Long patronId) {
        long activeHolds = holdRepository.countByPatronIdAndStatus(
            patronId, HoldStatus.ACTIVE
        );

        if (activeHolds >= MAX_BOOKS_PER_PATRON) {
            throw new BorrowingLimitExceededException(patronId);
        }
    }

    private LocalDate calculateDueDate() {
        return LocalDate.now().plusWeeks(2);
    }
}
```

##### 3. 通知服务模块

```java
// notification/internal/NotificationService.java
@Service
class NotificationService {

    private final EmailService emailService;
    private final SmsService smsService;
    private final PatronApi patronApi;

    @EventListener
    @Async
    public void handleBookBorrowed(BookBorrowedEvent event) {
        PatronDto patron = patronApi.findById(event.patronId())
            .orElse(null);

        if (patron != null) {
            sendBorrowConfirmation(patron, event);
        }
    }

    @EventListener
    @Async
    public void handleBookOverdue(BookOverdueEvent event) {
        PatronDto patron = patronApi.findById(event.patronId())
            .orElse(null);

        if (patron != null) {
            sendOverdueNotification(patron, event);
        }
    }

    private void sendBorrowConfirmation(PatronDto patron, BookBorrowedEvent event) {
        String subject = "图书借阅确认";
        String content = String.format(
            "您好 %s，您已成功借阅图书《%s》，请于 %s 前归还。",
            patron.name(),
            event.bookTitle(),
            event.dueDate()
        );

        emailService.sendEmail(patron.email(), subject, content);
    }

    private void sendOverdueNotification(PatronDto patron, BookOverdueEvent event) {
        String subject = "图书逾期提醒";
        String content = String.format(
            "您好 %s，您借阅的图书《%s》已逾期 %d 天，请尽快归还。",
            patron.name(),
            event.bookTitle(),
            event.overdueDays()
        );

        emailService.sendEmail(patron.email(), subject, content);

        // 如果逾期超过7天，发送短信提醒
        if (event.overdueDays() > 7) {
            smsService.sendSms(patron.phone(), content);
        }
    }
}
```

##### 4. 定时任务处理

```java
// borrow/internal/OverdueCheckService.java
@Service
class OverdueCheckService {

    private final HoldRepository holdRepository;
    private final ApplicationEventPublisher events;

    @Scheduled(cron = "0 0 9 * * ?") // 每天上午9点执行
    public void checkOverdueBooks() {
        LocalDate today = LocalDate.now();

        List<Hold> overdueHolds = holdRepository.findOverdueHolds(today);

        for (Hold hold : overdueHolds) {
            long overdueDays = ChronoUnit.DAYS.between(hold.getDueDate(), today);

            events.publishEvent(new BookOverdueEvent(
                hold.getIsbn(),
                hold.getPatronId(),
                hold.getDueDate(),
                (int) overdueDays
            ));
        }
    }
}
```

### 部署配置

#### 1. Docker 配置

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/library-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 2. Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  library-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/library
      - SPRING_DATASOURCE_USERNAME=library
      - SPRING_DATASOURCE_PASSWORD=password
    depends_on:
      - mysql
      - redis

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_DATABASE=library
      - MYSQL_USER=library
      - MYSQL_PASSWORD=password
      - MYSQL_ROOT_PASSWORD=rootpassword
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  mysql_data:
```

#### 3. 生产环境配置

```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

  task:
    execution:
      pool:
        core-size: 5
        max-size: 20
        queue-capacity: 100

logging:
  level:
    com.example.library: INFO
    org.springframework.modulith: INFO

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

## 🚀 总结

### Spring Modulith 的优势

1. **学习成本低**: 基于Spring Boot，容易上手
2. **工具支持好**: 自动验证、测试、文档生成
3. **渐进式采用**: 可以逐步重构现有项目
4. **部署简单**: 单体应用，部署运维简单
5. **性能优良**: 进程内调用，性能优于微服务

### 适用场景

- **中小型团队**: 人员规模10-50人
- **业务复杂度中等**: 需要模块化但不需要微服务
- **快速迭代**: 需要快速开发和部署
- **运维资源有限**: 不想维护复杂的微服务架构

### 与其他架构的对比

| 特性 | Spring Modulith | DDD 四层架构 | 微服务架构 |
|------|----------------|-------------|-----------|
| **复杂度** | 中 | 高 | 很高 |
| **学习成本** | 低 | 高 | 很高 |
| **部署复杂度** | 低 | 低 | 高 |
| **扩展性** | 中 | 中 | 高 |
| **性能** | 高 | 高 | 中 |
| **团队协作** | 好 | 中 | 优秀 |

Spring Modulith 是一个很好的中间方案，既保持了单体应用的简单性，又提供了模块化的好处。对于大多数中小型项目来说，这是一个非常实用的选择。
```
```
