package com.professionalbeginner.data.hibernate;

import com.professionalbeginner.TestUtils;
import com.professionalbeginner.domain.core.book.Book;
import com.professionalbeginner.domain.core.book.BookId;
import com.professionalbeginner.domain.core.book.Price;
import com.professionalbeginner.domain.core.review.Review;
import com.professionalbeginner.domain.core.review.ReviewId;
import com.professionalbeginner.domain.interfacelayer.repository.BookNotFoundException;
import com.professionalbeginner.domain.interfacelayer.repository.BookRepository;
import com.professionalbeginner.domain.interfacelayer.repository.ReviewRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Kempenich Florian
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-tests")
public class HibernateBookRepositoryIntegrationTest {

    // TODO: 12/10/2016 Also implement integration tests on service, or even controller

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TestUtils testUtils;

    @Test
    public void bookNotFound_throwException() throws Exception {
        BookId inexistingId = new BookId(123L);
        try {
            bookRepository.findById(inexistingId, true);
            fail("Should throw exception");
        } catch (BookNotFoundException e) {
            assertEquals("Book could not be found: " + inexistingId, e.getMessage());
            assertEquals(inexistingId, e.getSearchedId());
        }
    }

    @Test
    public void saveAndFind_withoutReviews() throws Exception {
        Book toSave = testUtils.makeRandomBook(BookId.NOT_ASSIGNED);
        assertEquals(BookId.NOT_ASSIGNED, toSave.id());

        BookId savedId = bookRepository.save(toSave);
        assertNotEquals(BookId.NOT_ASSIGNED, savedId); //Check that repository returns a correct id

        Book fromRepo = bookRepository.findById(savedId, false);

        assertEquals(savedId, fromRepo.id());
        assertTrue(areBooksSimilar_ignoreReviews(toSave, fromRepo));
    }

    @Test
    public void saveWithExistingId_updatesExisting() throws Exception {
        // Save book and get id
        Book toSave = testUtils.makeRandomBook(BookId.NOT_ASSIGNED);
        BookId savedId = bookRepository.save(toSave);
        toSave.setId(savedId);

        // Change state of book
        Price originalPrice = toSave.price();
        Price newPrice = new Price(originalPrice.amount() / 2);
        toSave.updatePrice(newPrice);

        // Get from repo before and after update
        Book fromRepo = bookRepository.findById(savedId, false);
        bookRepository.save(toSave);
        Book fromRepoAfterUpdate = bookRepository.findById(savedId, false);

        // Assert result
        assertEquals(originalPrice, fromRepo.price());
        assertEquals(newPrice, fromRepoAfterUpdate.price());
    }

    @Test
    public void saveAndFind_withReviews() throws Exception {
        // Get id
        Book toSave = testUtils.makeRandomBook(BookId.NOT_ASSIGNED);
        BookId savedId = bookRepository.save(toSave);
        toSave.setId(savedId);

        // Create and persist review
        List<Review> reviews = createAndPersistRandomReviews(savedId);

        // Add reviews and update version on repo
        reviews.forEach(toSave::addReview);
        bookRepository.save(toSave);

        Book fromRepo = bookRepository.findById(savedId, true);

        assertEquals(toSave.getReviews(), fromRepo.getReviews());
        assertTrue(areBooksSimilar_ignoreReviews(toSave, fromRepo));
    }

    @Test
    public void saveMultiple_findAll_withAndWithoutReviews() throws Exception {
        // Generate random books
        Book book1 = testUtils.makeRandomBook(BookId.NOT_ASSIGNED);
        Book book2 = testUtils.makeRandomBook(BookId.NOT_ASSIGNED);
        Book book3 = testUtils.makeRandomBook(BookId.NOT_ASSIGNED);
        List<Book> books = Arrays.asList(book1, book2, book3);

        // Save all books & update ids
        books.forEach(book -> {
            BookId id = bookRepository.save(book);
            book.setId(id);
        });

        // Add reviews
        List<Review> reviewsBook1 = createAndPersistRandomReviews(book1.id());
        List<Review> reviewsBook2 = createAndPersistRandomReviews(book2.id());
        List<Review> reviewsBook3 = createAndPersistRandomReviews(book3.id());
        reviewsBook1.forEach(book1::addReview);
        reviewsBook2.forEach(book2::addReview);
        reviewsBook3.forEach(book3::addReview);

        // Update books via Repo
        books.forEach(bookRepository::save);

        // Get all w/ and w/o Reviews
        List<Book> withReview = bookRepository.findAll(true);
        List<Book> withoutReview = bookRepository.findAll(false);


        // Assert List contain similar books w/ Reviews
        books.forEach(
                book -> assertListContainsSimilarBook_withReviews(book, withReview)
        );

        // Assert List contain similar books ignore Reviews, and check review list is empty
        books.forEach(
                book -> assertListContainsSimilarBook_ignoreReview(book, withoutReview)
        );
        withoutReview.forEach(
                bookFromDb -> assertTrue("Review list should be empty", bookFromDb.getReviews().isEmpty())
        );

    }

    @Test
    public void saveWithReview_findWithout_resultDoesntHaveReviews() throws Exception {
        // Save book
        Book book = testUtils.makeRandomBook(BookId.NOT_ASSIGNED);
        BookId savedId  = bookRepository.save(book);
        book.setId(savedId);

        // Add review and update on Repo
        List<Review> reviews = createAndPersistRandomReviews(savedId);
        reviews.forEach(book::addReview);
        bookRepository.save(book);

        // Retrieve book w/o Reviews
        Book fromRepo = bookRepository.findById(savedId, false);

        // Review list empty
        assertTrue("Review list should be empty", fromRepo.getReviews().isEmpty());
    }


    private List<Review> createAndPersistRandomReviews(BookId bookId) {
        Review review1 = testUtils.makeRandomReview(ReviewId.NOT_ASSIGNED, bookId);
        Review review2 = testUtils.makeRandomReview(ReviewId.NOT_ASSIGNED, bookId);
        Review review3 = testUtils.makeRandomReview(ReviewId.NOT_ASSIGNED, bookId);
        ReviewId id1 = reviewRepository.save(review1);
        ReviewId id2 = reviewRepository.save(review2);
        ReviewId id3 = reviewRepository.save(review3);
        review1.setId(id1);
        review2.setId(id2);
        review3.setId(id3);

        return Arrays.asList(review1, review2, review3);
    }

    private void assertListContainsSimilarBook_withReviews(Book toCheck, List<Book> bookList) {
        boolean contains = bookList.stream()
                .anyMatch(book -> areBooksSimilar(toCheck, book));
        assertTrue("List does not contain book: " + toCheck, contains);
    }

    private void assertListContainsSimilarBook_ignoreReview(Book toCheck, List<Book> bookList) {
        boolean contains = bookList.stream()
                .anyMatch(book -> areBooksSimilar_ignoreReviews(toCheck, book));
        assertTrue("List does not contain book: " + toCheck, contains);
    }

    private boolean areBooksSimilar(Book toCheck, Book book) {
        return areBooksSimilar_ignoreReviews(toCheck, book)
                && toCheck.getReviews().containsAll(book.getReviews())
                && book.getReviews().containsAll(toCheck.getReviews());
    }

    private boolean areBooksSimilar_ignoreReviews(Book toCheck, Book book) {
        return toCheck.characteristics().title().equals(book.characteristics().title()) &&
                toCheck.characteristics().author().equals(book.characteristics().author()) &&
                toCheck.characteristics().numPages() == book.characteristics().numPages() &&
                Double.compare(toCheck.price().amount(), book.price().amount()) == 0;
    }

}