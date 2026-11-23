package br.com.gestor360.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CatalogDtos {
    private CatalogDtos() { }

    public record CategoryRequest(@NotBlank(message = "Informe o nome da categoria.") @Size(max = 80, message = "O nome deve ter até 80 caracteres.") String name, @Size(max = 300, message = "A descrição deve ter até 300 caracteres.") String description) { }
    public record CategoryView(Long id, String name, String description) { }
    public record ProductRequest(@NotBlank(message = "Informe o código do produto.") @Size(max = 40, message = "O código deve ter até 40 caracteres.") String code,
                                 @NotBlank(message = "Informe o nome do produto.") @Size(max = 120, message = "O nome deve ter até 120 caracteres.") String name,
                                 @Size(max = 500, message = "A descrição deve ter até 500 caracteres.") String description,
                                 @Min(value = 0, message = "O preço não pode ser negativo.") long priceCents,
                                 @Min(value = 0, message = "O estoque mínimo não pode ser negativo.") int minimumStock, Long categoryId) { }
    public record ProductView(Long id, String code, String name, String description, long priceCents,
                              int stock, int minimumStock, boolean active, Long categoryId, String categoryName) { }
    public record PartyRequest(@NotBlank(message = "Informe o nome.") @Size(max = 120, message = "O nome deve ter até 120 caracteres.") String name,
                               @Email(message = "Informe um e-mail válido.") @Size(max = 160, message = "O e-mail deve ter até 160 caracteres.") String email,
                               @Size(max = 30, message = "O telefone deve ter até 30 caracteres.") String phone) { }
    public record PartyView(Long id, String name, String email, String phone) { }
}
