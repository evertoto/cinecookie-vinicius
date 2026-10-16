package br.com.cinecookie.review;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public class ReviewForm {
    @NotNull(message = "Escolha uma nota.")
    @DecimalMin(value = "0.5", message = "A nota mínima é 0,5.")
    @DecimalMax(value = "5.0", message = "A nota máxima é 5.")
    @Digits(integer = 1, fraction = 1, message = "Use notas de 0,5 em 0,5.")
    private BigDecimal rating;
    @Size(max = 1000, message = "O comentário deve ter no máximo 1000 caracteres.")
    private String comment = "";

    @AssertTrue(message = "Use notas de 0,5 em 0,5.")
    public boolean isRatingStepValid() {
        return rating == null || rating.remainder(new BigDecimal("0.5")).signum() == 0;
    }
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
