package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class InternshipApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ItemRepository itemRepository;

	@BeforeEach
	public void setUp() {
		//insert an item into database
		Item item = new Item(null, "Item 1", "Description 1", "NEW", "test1@example.com");
		itemRepository.save(item);
	}

	@Test
	public void testCreateItem_Success() throws Exception {
		Item item = new Item(null, "Item 1", "Description 1", "NEW", "test1@example.com");
		mockMvc.perform(post("/api/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(item)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Item 1"))
				.andExpect(jsonPath("$.description").value("Description 1"));
	}

	@Test
	public void testGetItemById_Found() throws Exception {
		//I assume here, that item with id 1 exists in the database (in setup I insert an item)
		mockMvc.perform(get("/api/items/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	public void testGetItemById_NotFound() throws Exception {
		mockMvc.perform(get("/api/items/999")) //I am assuming here that item with id 999 does not exist
				.andExpect(status().isNotFound());
	}

	@Test
	public void testUpdateItem_Success() throws Exception {
		//insert an item first
		Item savedItem = itemRepository.save(new Item(null, "Original", "Original description", "original", "original@gmail.com"));
		//then update the existing item
		Item updatedItem = new Item(savedItem.getId(), "Updated item", "Updated description", "updated", "updated@gmail.com");

		mockMvc.perform(put("/api/items/" + savedItem.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updatedItem)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated item"))
				.andExpect(jsonPath("$.description").value("Updated description"));
	}

	@Test
	public void testDeleteItem_Found() throws Exception {
		mockMvc.perform(delete("/api/items/1"))
				.andExpect(status().isNoContent());
	}

	@Test
	public void testDeleteItem_NotFound() throws Exception {
		mockMvc.perform(delete("/api/items/999"))
				.andExpect(status().isNotFound());
	}
}
