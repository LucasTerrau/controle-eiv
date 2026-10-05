package br.com.lucasterra.eiv.compartilhado;

public class ProcessoNaoEncontradoException extends RuntimeException {

    public ProcessoNaoEncontradoException(Long id) {
        super("Processo " + id + " não encontrado.");
    }
}
