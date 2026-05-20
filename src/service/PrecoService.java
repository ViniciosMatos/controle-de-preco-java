package service;

import domain.EntityInterface;
import domain.Preco;
import domain.Produto;
import infra.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.UUID;

public class PrecoService implements ServiceInterface{
    @Override
    public void add(EntityInterface entity) {
        Preco preco = (Preco) entity;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            session.persist(preco);
            tx.commit();
        }
    }

    @Override
    public void remove(EntityInterface entity) {

    }

    @Override
    public void list() {
        List<Preco> precos = listar();

        for (int i = 0; i < precos.size(); i++) {
            Preco preco = precos.get(i);
            System.out.printf("\nIndice: %s\n", i);
            System.out.printf("Id: %s\n", preco.getId());
            System.out.printf("Nome: %s\n", preco.getProduto().getNome());
            System.out.printf("Data: %s\n", preco.getDataAtual());
            System.out.printf("Preco: %s\n", preco.getPreco());
            System.out.printf("Loja: %s\n", preco.getNomeLoja());
            System.out.printf("Loja: %s\n", preco.getUrlProduto());
            IO.println("\n-----------------------------------\n");
        }
    }

    @Override
    public void edit(EntityInterface entity, UUID id) {

    }

    @Override
    public EntityInterface findByIndex(int index) {
        return null;
    }

    public List<Preco> listar(){
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            return session.createQuery("from Preco", Preco.class).getResultList();
        }
    }
}
