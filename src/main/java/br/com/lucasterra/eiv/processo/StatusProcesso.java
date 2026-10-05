package br.com.lucasterra.eiv.processo;

public enum StatusProcesso {
    EM_ANALISE,
    AGUARDANDO_DOCUMENTOS,
    APROVADO,
    REPROVADO;

    public boolean isConcluido() {
        return this == APROVADO || this == REPROVADO;
    }
}
