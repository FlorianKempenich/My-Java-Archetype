package com.professionalbeginner.presentation.model;

import com.google.common.base.MoreObjects;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import java.util.List;

/**
 * @author Kempenich Florian
 */
public class BookDTO {

    @Min(1)
    private Long bookId;

    @NotEmpty
    private String title;

    @NotEmpty
    private String author;

    @Min(1)
    private int numPages;

    @Min(0)
    private double price;

    private List<ReviewDTO> reviews;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getNumPages() {
        return numPages;
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<ReviewDTO> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDTO> reviews) {
        this.reviews = reviews;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bookId", bookId)
                .add("title", title)
                .add("author", author)
                .add("numPages", numPages)
                .add("price", price)
                .toString();
    }
}
