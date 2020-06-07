package alt.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import alt.backend.Cliente;
import alt.util.ConexaoJDBC;
import alt.util.ConexaoPostgresJDBC;

public class ClienteDAO {

	private final ConexaoJDBC connection;

	public ClienteDAO() throws SQLException, ClassNotFoundException {
		this.connection = new ConexaoPostgresJDBC();
	}

	public String save(Cliente cliente) throws SQLException, ClassNotFoundException {
		String id = null;

		String sql = "INSERT INTO cliente (cliente_id, cliente_nome, cliente_endereco, cliente_telefone, cliente_datanascimento) VALUES (?, ?, ?, ?, ?) RETURNING cliente_id;";
		PreparedStatement ps = null;
		try {
			ps = this.connection.getConnection().prepareStatement(sql);
			ps.setString(1, cliente.id);
			ps.setString(2, cliente.nome);
			ps.setString(3, cliente.endereco);
			ps.setString(4, cliente.telefone);
			ps.setString(5, cliente.dataNascimento);
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				id = rs.getString("cliente_id");
			}

			this.connection.commit();
		} catch (SQLException ex) {
			this.connection.rollback();
			Logger.getLogger(ClienteDAO.class.getName()).log(Level.SEVERE, null, ex);
			throw ex;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return id;
	}

	public void update(Cliente cliente) throws SQLException, ClassNotFoundException {
		String sql = "UPDATE cliente SET cliente_nome=?, cliente_endereco=?, cliente_telefone=?, cliente_datanascimento=? where cliente_id =?";
		PreparedStatement ps = null;
		try {
			ps = this.connection.getConnection().prepareStatement(sql);
			ps.setString(1, cliente.nome);
			ps.setString(2, cliente.endereco);
			ps.setString(3, cliente.telefone);
			ps.setString(4, cliente.dataNascimento);
			ps.setString(5, cliente.id);
			ps.execute();

			this.connection.commit();
		} catch (SQLException ex) {
			this.connection.rollback();
			Logger.getLogger(ClienteDAO.class.getName()).log(Level.SEVERE, null, ex);
			throw ex;
		} finally {
			ps.close();
		}
	}

	public int excluir(String id) throws SQLException, ClassNotFoundException {
		int linhasAfetadas = 0;
		String sql = "delete from cliente where cliente_id = ?";
		PreparedStatement ps = null;
		try {
			ps = this.connection.getConnection().prepareStatement(sql);
			ps.setString(1, id);
			linhasAfetadas = ps.executeUpdate();

			this.connection.commit();
		} catch (SQLException ex) {
			this.connection.rollback();
			if (ex.getSQLState().equals("23503")) {
				System.out.println(
						"ATENÇÃO!!! Não é Possivel deletar " + id + " pois está sendo usado(a) em outro Registro!");
			}
			Logger.getLogger(ClienteDAO.class.getName()).log(Level.SEVERE, null, ex);
			throw ex;
		} finally {
			ps.close();
		}
		return linhasAfetadas;
	}

	public List<Cliente> findAll() throws SQLException, ClassNotFoundException {

		StringBuffer sql = new StringBuffer(" SELECT * FROM cliente ORDER BY cliente_id;");

		PreparedStatement ps = null;
		ResultSet resultSet = null;
		List<Cliente> listClient = new ArrayList<Cliente>();
		try {
			ps = connection.getConnection().prepareStatement(sql.toString());
			resultSet = ps.executeQuery();
			while (resultSet.next()) {

				Cliente cliente = new Cliente();
				cliente.setId(resultSet.getString("cliente_id"));
				cliente.setNome(resultSet.getString("cliente_nome"));
				cliente.setEndereco(resultSet.getString("cliente_endereco"));
				cliente.setTelefone(resultSet.getString("cliente_telefone"));
				cliente.setDataNascimento(resultSet.getString("cliente_datanascimento"));

				listClient.add(cliente);
			}
			return (List<Cliente>) listClient;
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}

}
