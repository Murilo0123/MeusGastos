package br.unasp.meusgastos.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import br.unasp.meusgastos.model.Categoria
import br.unasp.meusgastos.model.Gasto
import br.unasp.meusgastos.model.GastoRepository
import br.unasp.meusgastos.model.GastoRepositoryDemo
import br.unasp.meusgastos.model.GastoRepositoryMemoria


class GastosViewModel(
    private val repositorio: GastoRepository = GastoRepositoryMemoria()
) : ViewModel() {

    // ---------- 1. O QUE A TELA PRECISA SABER ----------

    var descricao by mutableStateOf("")
        private set

    var valorTexto by mutableStateOf("")
        private set

    var categoriaSelecionada by mutableStateOf(Categoria.ALIMENTACAO)
        private set

    var erro: String? by mutableStateOf(null)
        private set

    private val _gastos = mutableStateListOf<Gasto>()
    val gastos: List<Gasto> = _gastos

    var filtro: Categoria? by mutableStateOf(null)
        private set


    val gastosVisiveis: List<Gasto>
        get() = if (filtro == null) gastos else gastos.filter { it.categoria == filtro }

    val total: Double
        get() = gastosVisiveis.sumOf { it.valor }

    val quantidade: Int
        get() = gastosVisiveis.size

    // ---------- 2. O QUE A TELA PODE PEDIR ----------

    init {
        sincronizar()
    }

    fun aoDigitarDescricao(novo: String) {
        descricao = novo; erro = null
    }

    fun aoDigitarValor(novo: String) {
        valorTexto = novo; erro = null
    }

    fun aoSelecionarCategoria(nova: Categoria) {
        categoriaSelecionada = nova
    }

    fun aoFiltrar(categoria: Categoria?) {
        filtro = categoria
    }

    fun adicionar() {
        val valor = valorTexto.replace(",", ".").toDoubleOrNull()

        erro = when {
            descricao.isBlank() -> "Informe a descrição do gasto"
            valor == null -> "O valor precisa ser um número"
            valor <= 0.0 -> "O valor precisa ser maior que zero"
            else -> null
        }
        if (erro != null) return

        repositorio.adicionar(descricao.trim(), valor!!, categoriaSelecionada)
        sincronizar()
        descricao = ""
        valorTexto = ""
    }

    fun remover(id: Long) {
        repositorio.remover(id)
        sincronizar()
    }

    private fun sincronizar() {
        _gastos.clear()
        _gastos.addAll(repositorio.listar())
    }
}