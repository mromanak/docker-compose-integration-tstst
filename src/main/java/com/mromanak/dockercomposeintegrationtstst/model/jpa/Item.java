package com.mromanak.dockercomposeintegrationtstst.model.jpa;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

// @EqualsAndHashCode (and by proxy @Data) does not play nice with JPA, so we get this mess of Lombok annotations.
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class Item {

    @Id
    private Long id;

    @NotBlank(message = "Name must be non-blank")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id)
                && Objects.equals(name, item.name)
                && Objects.equals(description, item.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description);
    }
}
