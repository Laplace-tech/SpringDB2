package hello.springdb2.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hello.springdb2.domain.Item;
import hello.springdb2.repository.ItemSearchCond;
import hello.springdb2.repository.ItemUpdateDto;
import hello.springdb2.service.ItemService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

	private final ItemService itemService;

	@GetMapping
	public String showitems(@ModelAttribute ItemSearchCond itemSearchCond, Model model) {
		List<Item> itemList = itemService.findItems(itemSearchCond);
		model.addAttribute("items", itemList);
		return "items";
	}

	@GetMapping("/{itemId}")
	public String item(@PathVariable("itemId") long itemId, Model model) {
		Item item = itemService.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("아이템이 존재하지 않음"));
		model.addAttribute("item", item);
		return "item";
	}

	@GetMapping("/add")
	public String addForm(@ModelAttribute Item item) {
		return "addForm";
	}

	@PostMapping("/add")
	public String addItem(Item item, RedirectAttributes redirectAttributes) {
		Item savedItem = itemService.save(item);
		redirectAttributes.addAttribute("itemId", savedItem.getId());
		redirectAttributes.addAttribute("status", true);
		return "redirect:/items/{itemId}";
	}

	@GetMapping("/{itemId}/edit")
	public String editForm(@PathVariable("itemId") Long itemId, Model model) {
		Item item = itemService.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("아이템이 존재하지 않음"));
		model.addAttribute("item", item);
		return "editForm";
	}

	@PostMapping("/{itemId}/edit")
	public String edit(@PathVariable("itemId") Long itemId, ItemUpdateDto updateParam) {
		itemService.update(itemId, updateParam);
		return "redirect:/items/{itemId}";
	}

}
