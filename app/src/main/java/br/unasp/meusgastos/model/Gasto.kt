package br.unasp.meusgastos.model

data class Gasto(
    val id: Long,
    val descricao: String,
    val valor: Double,
    val categoria: Categoria
)
