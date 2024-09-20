package it.univaq.example.webshop.business;

import it.univaq.example.webshop.model.Proposal;
import it.univaq.example.webshop.model.ProposalStateEnum;
import it.univaq.framework.data.DAO;
import it.univaq.framework.data.DataException;
import it.univaq.framework.data.DataItemProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import it.univaq.framework.data.DataLayer;
import it.univaq.framework.data.OptimisticLockException;

public class ProposalDAO extends DAO {

    private PreparedStatement sProposalByID, sProposalsByRequest, sLastProposalByRequest, sProposalsByTechnician, sProposalsByState, sProposalsByCreationMonth, iProposal, uProposal;

    public ProposalDAO(DataLayer d) {
        super(d);
    }

    
    public void init() throws DataException {
        try {
            super.init();
            sProposalByID = connection.prepareStatement("SELECT * FROM proposta WHERE id=?");
            sProposalsByRequest = connection.prepareStatement("SELECT id FROM proposta WHERE idRichiesta=?");
            sLastProposalByRequest = connection.prepareStatement("SELECT MAX(id) as id FROM proposta WHERE idRichiesta=?");
            sProposalsByTechnician = connection.prepareStatement("SELECT id FROM proposta WHERE idTecnico=?");
            sProposalsByState = connection.prepareStatement("SELECT id FROM proposta WHERE statoProposta=?");
            sProposalsByCreationMonth = connection.prepareStatement("SELECT id FROM proposta WHERE MONTH(dataCreazione)=? and YEAR(dataCreazione)=?");
            iProposal = connection.prepareStatement("INSERT INTO proposta (idRichiesta,idTecnico,nomeProdotto,nomeProduttore,descrizioneProdotto,prezzoProdotto,url,note,dataCreazione,statoProposta,motivazione) VALUES(?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            uProposal = connection.prepareStatement("UPDATE proposta SET idRichiesta=?,idTecnico=?,nomeProdotto=?,nomeProduttore=?,descrizioneProdotto=?,prezzoProdotto=?,url=?,note=?,dataCreazione=?,statoProposta=?,motivazione=?,versione=? WHERE id=? and versione=?");

        } catch (SQLException ex) {
            throw new DataException("Error initializing webshop data layer", ex);
        }
    }

    
    public void destroy() throws DataException {
        try {
            sProposalByID.close();
            sProposalsByRequest.close();
            sLastProposalByRequest.close();
            sProposalsByTechnician.close();
            sProposalsByState.close();
            sProposalsByCreationMonth.close();
            iProposal.close();
            uProposal.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        super.destroy();
    }

    
    public Proposal createProposal() {
        return new Proposal();
    }

    //helper
    private Proposal createProposal(ResultSet rs) throws DataException {
        Proposal a = (Proposal) createProposal();
        try {
            a.setKey(rs.getInt("id"));
            a.setRequestKey(rs.getInt("idRichiesta"));
            a.setTechnicianKey(rs.getInt("idTecnico"));
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
        } catch (SQLException ex) {
            throw new DataException("Unable to create proposal object form ResultSet", ex);
        }
        return a;
    }

    
    public Proposal getProposal(int proposal_key) throws DataException {
        Proposal a = null;
        if (dataLayer.getCache().has(Proposal.class, proposal_key)) {
            a = dataLayer.getCache().get(Proposal.class, proposal_key);
        } else {
            try {
                sProposalByID.setInt(1, proposal_key);
                try (ResultSet rs = sProposalByID.executeQuery()) {
                    if (rs.next()) {
                        a = createProposal(rs);
                        dataLayer.getCache().add(Proposal.class, a);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load proposal by ID", ex);
            }
        }
        return a;
    }

    
    public List<Proposal> getProposalsByRequest(int request_key) throws DataException {
        List<Proposal> result = new ArrayList<Proposal>();
        try {
            sProposalsByRequest.setInt(1, request_key);
            try (ResultSet rs = sProposalsByRequest.executeQuery()) {
                while (rs.next()) {
                    result.add((Proposal) getProposal(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load proposals by request", ex);
        }
        return result;
    }

    
    public Proposal getLastProposalByRequest(int request_key) throws DataException {
        try {
            sLastProposalByRequest.setInt(1, request_key);
            try (ResultSet rs = sLastProposalByRequest.executeQuery()) {
                if (rs.next()) {
                     return (Proposal) getProposal(rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load last proposal by request", ex);
        }
        return null;
    }

    
    public List<Proposal> getProposalsByTechnician(int user_key) throws DataException {
        List<Proposal> result = new ArrayList<Proposal>();
        try {
            sProposalsByTechnician.setInt(1, user_key);
            try (ResultSet rs = sProposalsByTechnician.executeQuery()) {
                while (rs.next()) {
                    result.add((Proposal) getProposal(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load proposals by technician", ex);
        }
        return result;
    }

    
    public List<Proposal> getProposalsByState(ProposalStateEnum value) throws DataException {
        List<Proposal> result = new ArrayList<Proposal>();
        try {
            sProposalsByState.setString(1, value.toString());
            try (ResultSet rs = sProposalsByState.executeQuery()) {
                while (rs.next()) {
                    result.add((Proposal) getProposal(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load proposals by state", ex);
        }
        return result;
    }

    
    public List<Proposal> getProposalsByCreationMonth(LocalDateTime date) throws DataException {
        List<Proposal> result = new ArrayList<Proposal>();
        try {
            sProposalsByCreationMonth.setInt(1, date.getMonthValue());
            sProposalsByCreationMonth.setInt(2, date.getYear());
            try (ResultSet rs = sProposalsByCreationMonth.executeQuery()) {
                while (rs.next()) {
                    result.add((Proposal) getProposal(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load proposals by creation date", ex);
        }
        return result;
    }

    
    public void setProposal(Proposal proposal) throws DataException {
        try {
            if (proposal.getKey() != null && proposal.getKey() > 0) { //update
                if (proposal instanceof DataItemProxy && !((DataItemProxy) proposal).isModified()) {
                    return;
                }
                if (proposal.getRequest() != null) {
                    uProposal.setInt(1, proposal.getRequest().getKey());
                } else {
                    uProposal.setNull(1, java.sql.Types.INTEGER);
                }                
                if (proposal.getTechnician() != null) {
                    uProposal.setInt(2, proposal.getTechnician().getKey());
                } else {
                    uProposal.setNull(2, java.sql.Types.INTEGER);
                }                       
                uProposal.setString(3, proposal.getProductName());
                uProposal.setString(4, proposal.getProducerName());
                uProposal.setString(5, proposal.getProductDescription());
                uProposal.setFloat(6, proposal.getProductPrice());
                uProposal.setString(7, proposal.getUrl());
                uProposal.setString(8, proposal.getNotes());
                uProposal.setObject(9, proposal.getCreationDate());
                uProposal.setString(10, proposal.getProposalState().toString());
                uProposal.setString(11, proposal.getMotivation());

                long current_version = proposal.getVersion();
                long next_version = current_version + 1;

                uProposal.setLong(12, next_version);
                uProposal.setInt(13, proposal.getKey());
                uProposal.setLong(14, current_version);

                if (uProposal.executeUpdate() == 0) {
                    throw new OptimisticLockException(proposal);
                } else {
                    proposal.setVersion(next_version);
                }
            } else { //insert
                if (proposal.getRequest() != null) {
                    iProposal.setInt(1, proposal.getRequest().getKey());
                } else {
                    iProposal.setNull(1, java.sql.Types.INTEGER);
                }                
                if (proposal.getTechnician() != null) {
                    iProposal.setInt(2, proposal.getTechnician().getKey());
                } else {
                    iProposal.setNull(2, java.sql.Types.INTEGER);
                }                       
                iProposal.setString(3, proposal.getProductName());
                iProposal.setString(4, proposal.getProducerName());
                iProposal.setString(5, proposal.getProductDescription());
                iProposal.setFloat(6, proposal.getProductPrice());
                iProposal.setString(7, proposal.getUrl());
                iProposal.setString(8, proposal.getNotes());
                iProposal.setObject(9, proposal.getCreationDate());
                iProposal.setString(10, proposal.getProposalState().toString());
                iProposal.setString(11, proposal.getMotivation());
                if (iProposal.executeUpdate() == 1) {
                    try (ResultSet keys = iProposal.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            proposal.setKey(key);
                            dataLayer.getCache().add(Proposal.class, proposal);
                        }
                    }
                }
            }
            if (proposal instanceof DataItemProxy) {
                ((DataItemProxy) proposal).setModified(false);
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to store proposal", ex);
        }
    }
}