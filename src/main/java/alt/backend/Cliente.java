package alt.backend;

import java.time.Instant;

import alt.vertx.ddd.PublicField;
import alt.vertx.memory.Identifiable;

public class Cliente implements Identifiable {
	
	@PublicField
	public String id;
	@PublicField
	public String nome;
	@PublicField
	public String endereco;
	@PublicField
	public String telefone;
	@PublicField
	public String dataNascimento;
	
	@Override
	public String getId() {
		return id;
	}
	@Override
	public void setId(String id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getEndereco() {
		return endereco;
	}
	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}
	public String getTelefone() {
		return telefone;
	}
	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}
	public String getDataNascimento() {
		return dataNascimento;
	}
	public void setDataNascimento(String dataNascimento) {
		this.dataNascimento = dataNascimento;
	}
	
	
	
	

}
