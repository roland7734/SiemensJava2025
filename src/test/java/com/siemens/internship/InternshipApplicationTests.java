package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InternshipApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private ItemService itemService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@Order(1)
	void contextLoads() {
		assertThat(itemService).isNotNull();
		assertThat(mockMvc).isNotNull();
	}

	@Test
	@Order(2)
	void testCreateItem_ValidEmail() throws Exception {
		ItemDTO dto = new ItemDTO("TestItem", "A test item", "test@example.com", "NEW");

		mockMvc.perform(post("/api/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("TestItem"))
				.andExpect(jsonPath("$.email").value("test@example.com"));
	}

	@Test
	@Order(3)
	void testCreateItem_InvalidEmail() throws Exception {
		ItemDTO dto = new ItemDTO("InvalidEmailItem", "bad", "invalid-email", "NEW");

		mockMvc.perform(post("/api/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isBadRequest());
	}


	@Test
	@Order(4)
	void testGetItemById_Found() throws Exception {
		Item saved = itemRepository.save(new Item(null, "FindMe", "desc", "NEW", "findme@example.com"));

		mockMvc.perform(get("/api/items/" + saved.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("FindMe"));
	}

	@Test
	@Order(5)
	void testGetItemById_NotFound() throws Exception {
		mockMvc.perform(get("/api/items/999999"))
				.andExpect(status().isNotFound());
	}

	@Test
	@Order(6)
	void testUpdateItem_Success() throws Exception {
		Item item = itemRepository.save(new Item(null, "BeforeUpdate", "desc", "NEW", "update@example.com"));
		ItemDTO dto = new ItemDTO("AfterUpdate", "desc", "update@example.com", "UPDATED");

		mockMvc.perform(put("/api/items/" + item.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("AfterUpdate"));
	}

	@Test
	@Order(7)
	void testUpdateItem_NotFound() throws Exception {
		ItemDTO dto = new ItemDTO("DoesNotExist", "desc", "notfound@example.com", "UPDATED");

		mockMvc.perform(put("/api/items/999999")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isNotFound());
	}

	@Test
	@Order(8)
	void testDeleteItem_Success() throws Exception {
		Item item = itemRepository.save(new Item(null, "DeleteMe", "desc", "NEW", "delete@example.com"));

		mockMvc.perform(delete("/api/items/" + item.getId()))
				.andExpect(status().isNoContent());
	}

	@Test
	@Order(9)
	void testDeleteItem_NotFound() throws Exception {
		mockMvc.perform(delete("/api/items/999999"))
				.andExpect(status().isNotFound());
	}

	@Test
	@Order(10)
	void testGetAllItems() throws Exception {
		itemRepository.save(new Item(null, "ListItem1", "desc", "NEW", "a@example.com"));
		itemRepository.save(new Item(null, "ListItem2", "desc", "NEW", "b@example.com"));

		mockMvc.perform(get("/api/items"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)));
	}

	@Test
	@Order(11)
	void testProcessItemsAsync() throws Exception {
		itemRepository.save(new Item(null, "ToProcess1", "desc", "NEW", "p1@example.com"));
		itemRepository.save(new Item(null, "ToProcess2", "desc", "NEW", "p2@example.com"));

		mockMvc.perform(get("/api/items/process"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
				.andExpect(jsonPath("$[0].status").value("PROCESSED"));
	}

	@Test
	@Order(12)
	void testService_processItemsAsync_Directly() {
		itemRepository.save(new Item(null, "ServiceAsync", "desc", "NEW", "sasync@example.com"));

		List<Item> processed = itemService.processItemsAsync();

		assertThat(processed).isNotEmpty();
		assertThat(processed.get(0).getStatus()).isEqualTo("PROCESSED");
	}
	@Test
	@Order(13)
	void testCreateItem_BlankName_ShouldFailValidation() throws Exception {
		ItemDTO dto = new ItemDTO("", "desc", "valid@email.com", "NEW");

		mockMvc.perform(post("/api/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@Order(14)
	void testCreateItem_InvalidJson_ShouldReturnBadRequest() throws Exception {
		String malformedJson = "{ \"name\": \"BadJson\", \"email\": \"noquote.com }"; // Broken JSON

		mockMvc.perform(post("/api/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content(malformedJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	@Order(15)
	void testCreateItem_DuplicateFields_DifferentID_ShouldSucceed() throws Exception {
		ItemDTO dto = new ItemDTO("DupItem", "desc", "dup@example.com", "NEW");

		// Create first
		mockMvc.perform(post("/api/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated());

		// Create second with same content — should still succeed (not unique constraint violation)
		mockMvc.perform(post("/api/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated());
	}

	@Test
	@Order(16)
	void testUpdateItem_WithMissingName_ShouldFailValidation() throws Exception {
		Item item = itemRepository.save(new Item(null, "Original", "desc", "NEW", "ok@example.com"));

		ItemDTO dto = new ItemDTO("", "desc", "ok@example.com", "UPDATED");

		mockMvc.perform(put("/api/items/" + item.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@Order(17)
	void testDeleteItem_AlreadyDeleted() throws Exception {
		Item item = itemRepository.save(new Item(null, "DelOnce", "desc", "NEW", "x@example.com"));

		// Delete first time
		mockMvc.perform(delete("/api/items/" + item.getId()))
				.andExpect(status().isNoContent());

		// Delete again — should now return 404
		mockMvc.perform(delete("/api/items/" + item.getId()))
				.andExpect(status().isNotFound());
	}

	@Test
	@Order(18)
	void testProcessItemsAsync_NoDuplicatesInResult() {
		itemRepository.save(new Item(null, "NoDup1", "desc", "NEW", "a1@ex.com"));
		itemRepository.save(new Item(null, "NoDup2", "desc", "NEW", "a2@ex.com"));
		itemRepository.save(new Item(null, "NoDup3", "desc", "NEW", "a3@ex.com"));

		List<Item> processed = itemService.processItemsAsync();

		// Ensure no duplicates in processed items
		long distinctCount = processed.stream()
				.map(Item::getId)
				.distinct()
				.count();

		assertThat(distinctCount).isEqualTo(processed.size());
	}

}
