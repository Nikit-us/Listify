package org.nikita.listify.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "images")
@Getter
@Setter
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_path_or_url", nullable = false, length = 512)
    private String filePathOrUrl;

    @Column(name = "is_preview", nullable = false)
    private boolean isPreview = false;

    @Column(name = "uploaded_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @Temporal(TemporalType.TIMESTAMP)
    private OffsetDateTime uploadedAt;

    // Связь с Advertisement (Много Изображений -> Одно Объявление)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "advertisement_id", nullable = false)
    private Advertisement advertisement;

    @PrePersist
    protected void onUpload() {
        this.uploadedAt = OffsetDateTime.now();
    }

    public Image() {}
    // equals, hashCode, toString (аналогично)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return id != null && Objects.equals(id, image.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Image{" +
                "id=" + id +
                ", filePathOrUrl='" + filePathOrUrl + '\'' +
                ", isPreview=" + isPreview +
                ", advertisementId=" + (advertisement != null ? advertisement.getId() : "null") +
                '}';
    }
}