package com.tech.listify.dto.categorydto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Узел в иерархическом дереве категорий")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CategoryTreeDto {

    @Schema(description = "ID категории", example = "1")
    private Integer id;

    @Schema(description = "Название категории", example = "Электроника")
    private String name;

    @Schema(description = "Список дочерних категорий")
    private List<CategoryTreeDto> children;
}