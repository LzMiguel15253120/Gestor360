package br.com.gestor360.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.gestor360.dto.CatalogDtos.*;
import br.com.gestor360.dto.OperationDtos.SaleItemView;
import br.com.gestor360.dto.OperationDtos.SaleView;
import br.com.gestor360.entity.*;
import br.com.gestor360.exception.BusinessRuleException;
import br.com.gestor360.exception.ResourceNotFoundException;
import br.com.gestor360.repository.*;

@Service
@Transactional
public class CatalogService {
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final CustomerRepository customers;
    private final SupplierRepository suppliers;
    private final SaleRepository sales;
    private final PurchaseRepository purchases;

    public CatalogService(CategoryRepository categories, ProductRepository products, CustomerRepository customers, SupplierRepository suppliers,
                          SaleRepository sales, PurchaseRepository purchases) {
        this.categories = categories; this.products = products; this.customers = customers; this.suppliers = suppliers;
        this.sales = sales; this.purchases = purchases;
    }

    @Transactional(readOnly = true)
    public List<CategoryView> categories() { return categories.findAll().stream().map(this::view).toList(); }
    public CategoryView createCategory(CategoryRequest request) {
        if (categories.existsByNameIgnoreCase(request.name().trim())) throw new BusinessRuleException("Já existe uma categoria com esse nome.");
        return view(categories.save(new Category(clean(request.name()), clean(request.description()))));
    }
    public CategoryView updateCategory(long id, CategoryRequest request) {
        Category category = category(id);
        if (!category.getName().equalsIgnoreCase(request.name().trim()) && categories.existsByNameIgnoreCase(request.name().trim()))
            throw new BusinessRuleException("Já existe uma categoria com esse nome.");
        category.update(clean(request.name()), clean(request.description()));
        return view(category);
    }
    public void deleteCategory(long id) {
        Category category = category(id);
        if (products.existsByCategoryId(id)) throw new BusinessRuleException("A categoria está vinculada a produtos e não pode ser excluída.");
        categories.delete(category);
    }

    @Transactional(readOnly = true)
    public List<ProductView> products() { return products.findAllByOrderByNameAsc().stream().map(this::view).toList(); }
    @Transactional(readOnly = true)
    public ProductView product(long id) { return view(findProduct(id)); }
    public ProductView createProduct(ProductRequest request) {
        if (products.existsByCodeIgnoreCase(request.code().trim())) throw new BusinessRuleException("Já existe um produto com esse código.");
        Category category = request.categoryId() == null ? null : category(request.categoryId());
        return view(products.save(new Product(clean(request.code()), clean(request.name()), clean(request.description()),
                request.priceCents(), 0, request.minimumStock(), category)));
    }
    public ProductView updateProduct(long id, ProductRequest request) {
        Product product = findProduct(id);
        if (!product.getCode().equalsIgnoreCase(request.code().trim()) && products.existsByCodeIgnoreCase(request.code().trim()))
            throw new BusinessRuleException("Já existe um produto com esse código.");
        Category category = request.categoryId() == null ? null : category(request.categoryId());
        product.update(clean(request.code()), clean(request.name()), clean(request.description()), request.priceCents(), request.minimumStock(), category);
        return view(product);
    }
    public void deactivateProduct(long id) { findProduct(id).deactivate(); }
    public void activateProduct(long id) { findProduct(id).activate(); }

    @Transactional(readOnly = true)
    public List<PartyView> customers() { return customers.findAll().stream().map(this::view).toList(); }
    public PartyView createCustomer(PartyRequest request) { return view(customers.save(new Customer(clean(request.name()), clean(request.email()), clean(request.phone())))); }
    public PartyView updateCustomer(long id, PartyRequest request) {
        Customer customer = customers.findById(id).orElseThrow(() -> notFound("Cliente", id));
        customer.update(clean(request.name()), clean(request.email()), clean(request.phone())); return view(customer);
    }
    public List<SaleView> customerHistory(long id) {
        if (!customers.existsById(id)) throw notFound("Cliente", id);
        return sales.findByCustomerIdOrderByCreatedAtDesc(id).stream().map(s -> new SaleView(s.getId(), id,
                s.getCustomer().getName(), s.getTotalCents(), s.getPaymentMethod(), s.getStatus(), false, s.getCreatedAt(),
                s.getItems().stream().map(i -> new SaleItemView(i.getProduct().getId(), i.getProductName(), i.getQuantity(),
                        i.getUnitPriceCents(), Math.multiplyExact(i.getUnitPriceCents(), i.getQuantity()))).toList())).toList();
    }
    public void deleteCustomer(long id) {
        Customer customer = customers.findById(id).orElseThrow(() -> notFound("Cliente", id));
        if (sales.existsByCustomerId(id)) throw new BusinessRuleException("O cliente tem vendas vinculadas e não pode ser excluído.");
        customers.delete(customer);
    }

    @Transactional(readOnly = true)
    public List<PartyView> suppliers() { return suppliers.findAll().stream().map(this::view).toList(); }
    public PartyView createSupplier(PartyRequest request) { return view(suppliers.save(new Supplier(clean(request.name()), clean(request.email()), clean(request.phone())))); }
    public PartyView updateSupplier(long id, PartyRequest request) {
        Supplier supplier = suppliers.findById(id).orElseThrow(() -> notFound("Fornecedor", id));
        supplier.update(clean(request.name()), clean(request.email()), clean(request.phone())); return view(supplier);
    }
    public void deleteSupplier(long id) {
        Supplier supplier = suppliers.findById(id).orElseThrow(() -> notFound("Fornecedor", id));
        if (purchases.existsBySupplierId(id)) throw new BusinessRuleException("O fornecedor tem compras vinculadas e não pode ser excluído.");
        suppliers.delete(supplier);
    }

    private Category category(long id) { return categories.findById(id).orElseThrow(() -> notFound("Categoria", id)); }
    private Product findProduct(long id) { return products.findById(id).orElseThrow(() -> notFound("Produto", id)); }
    private CategoryView view(Category c) { return new CategoryView(c.getId(), c.getName(), c.getDescription()); }
    private ProductView view(Product p) { return new ProductView(p.getId(), p.getCode(), p.getName(), p.getDescription(), p.getPriceCents(), p.getStock(), p.getMinimumStock(), p.isActive(), p.getCategory() == null ? null : p.getCategory().getId(), p.getCategory() == null ? null : p.getCategory().getName()); }
    private PartyView view(Customer p) { return new PartyView(p.getId(), p.getName(), p.getEmail(), p.getPhone()); }
    private PartyView view(Supplier p) { return new PartyView(p.getId(), p.getName(), p.getEmail(), p.getPhone()); }
    private ResourceNotFoundException notFound(String type, long id) { return new ResourceNotFoundException(type + " " + id + " não encontrado."); }
    private String clean(String value) { return value == null ? null : value.trim(); }
}
