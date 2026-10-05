package br.com.lucasterra.eiv.processo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProcessoEivRepository extends JpaRepository<ProcessoEiv, Long> {

    List<ProcessoEiv> findAllByOrderByDataVencimentoAsc();

    List<ProcessoEiv> findByStatusOrderByDataVencimentoAsc(StatusProcesso status);

    List<ProcessoEiv> findByStatusAndDataVencimentoLessThanEqualOrderByDataVencimentoAsc(
            StatusProcesso status, LocalDate limite);

    boolean existsByNumeroProtocolo(String numeroProtocolo);

    boolean existsByNumeroProtocoloAndIdNot(String numeroProtocolo, Long id);
}
