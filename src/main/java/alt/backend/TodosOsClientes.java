package alt.backend;

import java.sql.SQLException;

import alt.dao.ClienteDAO;
import alt.vertx.memory.ClienteRepository;
import io.vertx.ext.web.Router;

public class TodosOsClientes extends ClienteRepository<Cliente>{

	public TodosOsClientes(Router router) throws ClassNotFoundException, SQLException {
		super("/clientes", router);
		//this.save();
	}
	
	public void save() throws ClassNotFoundException, SQLException {
		Cliente cliente = new Cliente();
		cliente.setNome("nubia");
		cliente.setEndereco("rua das flores 75");
		cliente.setTelefone("(35)9.9706-7711");
		
		ClienteDAO clienteDAO = new ClienteDAO();
		clienteDAO.save(cliente);
		
	}
	
	

}
