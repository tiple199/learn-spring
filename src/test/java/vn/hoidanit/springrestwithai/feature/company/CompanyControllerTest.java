package vn.hoidanit.springrestwithai.feature.company;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import vn.hoidanit.springrestwithai.common.TestDataFactory;
import vn.hoidanit.springrestwithai.feature.company.dto.CreateCompanyRequest;
import vn.hoidanit.springrestwithai.feature.company.dto.UpdateCompanyRequest;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        testDataFactory.seedPermissions("COMPANIES", "/api/v1/companies", "GET", "POST", "PUT", "DELETE");
        testDataFactory.seedPermissions("COMPANIES", "/api/v1/companies/**", "GET", "DELETE");
    }

    @AfterEach
    void cleanUp() {
        companyRepository.deleteAll();
        testDataFactory.cleanup();
    }

    // ========== POST /api/v1/companies ==========

    @Test
    @DisplayName("POST /companies - 201: creates company and returns response body")
    void createCompany_success_returns201() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest(
                "HoiDanIT", "Education platform", "Ho Chi Minh City", "/logos/hoidanit.png");

        mockMvc.perform(post("/api/v1/companies")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.name", is("HoiDanIT")))
                .andExpect(jsonPath("$.data.description", is("Education platform")))
                .andExpect(jsonPath("$.data.address", is("Ho Chi Minh City")))
                .andExpect(jsonPath("$.data.logo", is("/logos/hoidanit.png")))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    @DisplayName("POST /companies - 400: blank name returns validation error")
    void createCompany_blankName_returns400() throws Exception {
        String emptyJson = "{}";

        mockMvc.perform(post("/api/v1/companies")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("POST /companies - 409: duplicate name returns conflict")
    void createCompany_duplicateName_returns409() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest(
                "HoiDanIT", null, null, null);

        mockMvc.perform(post("/api/v1/companies")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/v1/companies")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode", is(409)));
    }

    @Test
    @DisplayName("POST /companies - 401: no token returns unauthorized")
    void createCompany_noToken_returns401() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest(
                "HoiDanIT", null, null, null);

        mockMvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /companies - 403: no permission returns forbidden")
    void createCompany_noPermission_returns403() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest(
                "HoiDanIT", null, null, null);

        mockMvc.perform(post("/api/v1/companies")
                        .with(testDataFactory.jwtWithoutPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========== GET /api/v1/companies/{id} ==========

    @Test
    @DisplayName("GET /companies/{id} - 200: returns company")
    void getById_found_returns200() throws Exception {
        Company saved = companyRepository.save(
                buildCompany("HoiDanIT", "Education platform", "HCM", "/logos/hoidanit.png"));

        mockMvc.perform(get("/api/v1/companies/{id}", saved.getId())
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is("HoiDanIT")));
    }

    @Test
    @DisplayName("GET /companies/{id} - 404: not found returns 404")
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/companies/{id}", 99999L)
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("GET /companies/{id} - 401: no token returns unauthorized")
    void getById_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/companies/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /companies/{id} - 403: no permission returns forbidden")
    void getById_noPermission_returns403() throws Exception {
        Company saved = companyRepository.save(
                buildCompany("HoiDanIT", null, null, null));

        mockMvc.perform(get("/api/v1/companies/{id}", saved.getId())
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== GET /api/v1/companies ==========

    @Test
    @DisplayName("GET /companies - 200: returns paginated list")
    void getAll_returns200WithPaginationMetadata() throws Exception {
        companyRepository.save(buildCompany("HoiDanIT", null, null, null));
        companyRepository.save(buildCompany("FPT Software", null, null, null));

        mockMvc.perform(get("/api/v1/companies")
                        .with(testDataFactory.jwtWithPermission())
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.meta.page", is(1)))
                .andExpect(jsonPath("$.data.meta.pageSize", is(10)))
                .andExpect(jsonPath("$.data.meta.total", is(2)))
                .andExpect(jsonPath("$.data.meta.pages", is(1)))
                .andExpect(jsonPath("$.data.result", notNullValue()));
    }

    @Test
    @DisplayName("GET /companies - 403: no permission returns forbidden")
    void getAll_noPermission_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/companies")
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== PUT /api/v1/companies ==========

    @Test
    @DisplayName("PUT /companies - 200: updates and returns updated company")
    void updateCompany_success_returns200() throws Exception {
        Company saved = companyRepository.save(
                buildCompany("Old Name", "Old desc", "Old addr", null));

        UpdateCompanyRequest request = new UpdateCompanyRequest(
                saved.getId(), "New Name", "New desc", "New addr", "/logos/new.png");

        mockMvc.perform(put("/api/v1/companies")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.name", is("New Name")))
                .andExpect(jsonPath("$.data.description", is("New desc")))
                .andExpect(jsonPath("$.data.address", is("New addr")));
    }

    @Test
    @DisplayName("PUT /companies - 400: null id returns validation error")
    void updateCompany_nullId_returns400() throws Exception {
        UpdateCompanyRequest request = new UpdateCompanyRequest(
                null, "Some Name", null, null, null);

        mockMvc.perform(put("/api/v1/companies")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("PUT /companies - 404: non-existent id returns 404")
    void updateCompany_notFound_returns404() throws Exception {
        UpdateCompanyRequest request = new UpdateCompanyRequest(
                99999L, "Some Name", null, null, null);

        mockMvc.perform(put("/api/v1/companies")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("PUT /companies - 403: no permission returns forbidden")
    void updateCompany_noPermission_returns403() throws Exception {
        UpdateCompanyRequest request = new UpdateCompanyRequest(
                99999L, "Some Name", null, null, null);

        mockMvc.perform(put("/api/v1/companies")
                        .with(testDataFactory.jwtWithoutPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========== DELETE /api/v1/companies/{id} ==========

    @Test
    @DisplayName("DELETE /companies/{id} - 200: deletes company successfully")
    void deleteCompany_success_returns200() throws Exception {
        Company saved = companyRepository.save(
                buildCompany("HoiDanIT", null, null, null));

        mockMvc.perform(delete("/api/v1/companies/{id}", saved.getId())
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)));
    }

    @Test
    @DisplayName("DELETE /companies/{id} - 404: not found returns 404")
    void deleteCompany_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/companies/{id}", 99999L)
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("DELETE /companies/{id} - 403: no permission returns forbidden")
    void deleteCompany_noPermission_returns403() throws Exception {
        Company saved = companyRepository.save(
                buildCompany("HoiDanIT", null, null, null));

        mockMvc.perform(delete("/api/v1/companies/{id}", saved.getId())
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== helpers ==========

    private Company buildCompany(String name, String description, String address, String logo) {
        Company c = new Company();
        c.setName(name);
        c.setDescription(description);
        c.setAddress(address);
        c.setLogo(logo);
        return c;
    }
}
