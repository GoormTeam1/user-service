package edu.goorm.userservice.domain.user.dto;

import edu.goorm.userservice.domain.user.entity.Category;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategoryListRequestDto {
  private List<Category> categoryList;



}
