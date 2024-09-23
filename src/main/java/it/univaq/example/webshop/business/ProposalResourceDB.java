package it.univaq.example.webshop.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import it.univaq.example.webshop.model.Proposal;
import it.univaq.example.webshop.model.ProposalStateEnum;
import it.univaq.framework.exceptions.RESTWebApplicationException;

public class ProposalResourceDB {

    private static final String DS_NAME = "java:comp/env/jdbc/webshopdb";
    private static final String sProposalByID = "SELECT * FROM proposta WHERE id=?";
    private static final String sProposalsByRequest = "SELECT id FROM proposta WHERE idRichiesta=?";
    private static final String sLastProposalByRequest = "SELECT MAX(id) as id FROM proposta WHERE idRichiesta=?";
    private static final String sProposalsByTechnician = "SELECT id FROM proposta WHERE idTecnico=?";
    private static final String sProposalsByState = "SELECT id FROM proposta WHERE statoProposta=?";
    private static final String sProposalsByCreationMonth = "SELECT id FROM proposta WHERE MONTH(dataCreazione)=? and YEAR(dataCreazione)=?";
    private static final String iProposal = "INSERT INTO proposta (idRichiesta,idTecnico,nomeProdotto,nomeProduttore,descrizioneProdotto,prezzoProdotto,url,note,dataCreazione,statoProposta,motivazione) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
    private static final String uProposal = "UPDATE proposta SET idRichiesta=?,idTecnico=?,nomeProdotto=?,nomeProduttore=?,descrizioneProdotto=?,prezzoProdotto=?,url=?,note=?,dataCreazione=?,statoProposta=?,motivazione=?,versione=? WHERE id=? and versione=?";

    private static Connection getPooledConnection() throws NamingException, SQLException {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(DS_NAME);
        return ds.getConnection();
    }


    public static Proposal createProposal() {
        return new Proposal();
    }

    private static Proposal createProposal(ResultSet rs) throws RESTWebApplicationException {
        try {
            Proposal a = createProposal();
            a.setKey(rs.getInt("id"));
            a.setRequest(RequestResourceDB.getRequest(rs.getInt("idRichiesta")));
            a.setTechnician(UserResourceDB.getUser(rs.getInt("idTecnico")));
            a.setProductName(rs.getString("nomeProdotto"));
            a.setProducerName(rs.getString("nomeProduttore"));
            a.setProductDescription(rs.getString("descrizioneProdotto"));
            a.setProductPrice(rs.getFloat("prezzoProdotto"));
            a.setUrl(rs.getString("url"));
            a.setNotes(rs.getString("note"));
            a.setCreationDate(rs.getObject("dataCreazione", LocalDateTime.class));
            a.setProposalState(ProposalStateEnum.valueOf(rs.getString("statoProposta")));
            a.setMotivation(rs.getString("motivazione"));
            a.setVersion(rs.getLong("versione"));
            return a;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("Unable to create request object form ResultSet: " + ex.getMessage());
        }
    }

    public static Proposal getProposal(int proposal_key) throws RESTWebApplicationException {
        Proposal l = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sProposalByID)) {
                ps.setInt(1, proposal_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        l = createProposal(rs);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return l;
    }   

    
    public static List<Proposal> getProposalsByRequest(int request_key) throws RESTWebApplicationException {
        try {
            List<Proposal> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sProposalsByRequest)) {
                ps.setInt(1, request_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getProposal(rs.getInt("id")));
                    }
                }
            }
            return l;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static Proposal getLastProposalByRequest(int request_key) throws RESTWebApplicationException {
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sLastProposalByRequest)) {
            ps.setInt(1, request_key);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getProposal(rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return null;
    }

    public static List<Proposal> getProposalsByTechnician(int user_key) throws RESTWebApplicationException {
        try {
            List<Proposal> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sProposalsByTechnician)) {
                ps.setInt(1, user_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getProposal(rs.getInt("id")));
                    }
                }
            }
            return l;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static List<Proposal> getProposalsByState(ProposalStateEnum value) throws RESTWebApplicationException {
        try {
            List<Proposal> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sProposalsByState)) {
                ps.setString(1, value.toString());
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getProposal(rs.getInt("id")));
                    }
                }
            }
            return l;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static List<Proposal> getProposalsByCreationMonth(int month, int year) throws RESTWebApplicationException {
        try {
            List<Proposal> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sProposalsByCreationMonth)) {
                ps.setInt(1, month);
                ps.setInt(2, year);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getProposal(rs.getInt("id")));
                    }
                }
            }
            return l;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static Proposal setProposal(Proposal proposal) throws RESTWebApplicationException {
        try {
            if (proposal.getKey() != null && proposal.getKey() > 0) { //update
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(uProposal)) {
                    if (proposal.getRequest() != null) {
                        ps.setInt(1, proposal.getRequest().getKey());
                    } else {
                        ps.setNull(1, java.sql.Types.INTEGER);
                    }                
                    if (proposal.getTechnician() != null) {
                        ps.setInt(2, proposal.getTechnician().getKey());
                    } else {
                        ps.setNull(2, java.sql.Types.INTEGER);
                    }                       
                    ps.setString(3, proposal.getProductName());
                    ps.setString(4, proposal.getProducerName());
                    ps.setString(5, proposal.getProductDescription());
                    ps.setFloat(6, proposal.getProductPrice());
                    ps.setString(7, proposal.getUrl());
                    ps.setString(8, proposal.getNotes());
                    ps.setObject(9, proposal.getCreationDate());
                    ps.setString(10, proposal.getProposalState().toString());
                    ps.setString(11, proposal.getMotivation());

                    long current_version = proposal.getVersion();
                    long next_version = current_version + 1;

                    ps.setLong(12, next_version);
                    ps.setInt(13, proposal.getKey());
                    ps.setLong(14, current_version);

                    if (ps.executeUpdate() == 0) {
                        throw new RESTWebApplicationException("Proposal not updated");
                    } else {
                        proposal.setVersion(next_version);
                    }
                }
            } else { //insert
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(iProposal, Statement.RETURN_GENERATED_KEYS)) {
                    if (proposal.getRequest() != null) {
                        ps.setInt(1, proposal.getRequest().getKey());
                    } else {
                        ps.setNull(1, java.sql.Types.INTEGER);
                    }                
                    if (proposal.getTechnician() != null) {
                        ps.setInt(2, proposal.getTechnician().getKey());
                    } else {
                        ps.setNull(2, java.sql.Types.INTEGER);
                    }                       
                    ps.setString(3, proposal.getProductName());
                    ps.setString(4, proposal.getProducerName());
                    ps.setString(5, proposal.getProductDescription());
                    ps.setFloat(6, proposal.getProductPrice());
                    ps.setString(7, proposal.getUrl());
                    ps.setString(8, proposal.getNotes());
                    ps.setObject(9, proposal.getCreationDate());
                    ps.setString(10, proposal.getProposalState().toString());
                    ps.setString(11, proposal.getMotivation());

                    if (ps.executeUpdate() == 1) {
                        try ( ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                int key = keys.getInt(1);
                                proposal.setKey(key);
                            }
                        }
                    }
                }
            }
            return proposal;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }
}