package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.OriginalDocument;

import java.util.List;
import java.util.Optional;

@Repository
public interface OriginalDocumentRepository extends JpaRepository<OriginalDocument, Long> {

    Optional<OriginalDocument> findByDocumentCode(String documentCode);

    List<OriginalDocument> findByTransaction_Id(Long transactionId);

    @Query("SELECT o FROM OriginalDocument o WHERE " +
           "(:transactionId IS NULL OR o.transaction.id = :transactionId) AND " +
           "(:unlinkedOnly = false OR o.transaction IS NULL) AND " +
           "(:documentCode IS NULL OR o.documentCode LIKE %:documentCode%)")
    Page<OriginalDocument> findByFilters(@Param("transactionId") Long transactionId,
                                         @Param("unlinkedOnly") boolean unlinkedOnly,
                                         @Param("documentCode") String documentCode,
                                         Pageable pageable);

    @Query(value = "SELECT o.document_code FROM original_documents o WHERE o.document_code LIKE :prefix% ORDER BY o.document_code DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLastDocumentCodeByPrefix(@Param("prefix") String prefix);
}
