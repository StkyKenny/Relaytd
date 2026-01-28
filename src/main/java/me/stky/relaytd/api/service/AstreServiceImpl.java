package me.stky.relaytd.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreDTO;
import me.stky.relaytd.api.model.AstreID;
import me.stky.relaytd.api.repository.AstreRepository;
import me.stky.relaytd.config.SpringDataConfig;
import org.hibernate.query.SortDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class AstreServiceImpl implements AstreService {

    private final AstreRepository astreRepository;


    @Autowired
    public AstreServiceImpl(AstreRepository astreRepository) {
        this.astreRepository = astreRepository;
    }


    /**
     * @return
     * @throws ResponseStatusException When too many data will be loaded, use pagination in this case
     */
    @Override
    public List<Astre> getAllAstre() throws ResponseStatusException {
        long rowCounts = astreRepository.count();
        if (rowCounts > SpringDataConfig.MAX_UNPAGED_ROWS) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Too many rows : " + rowCounts + ", use paginated.");
        }
        return astreRepository.findAll();
    }

    public Page<Astre> getPaginatedAstres(int pageNumber, int size, String sortBy, String order) {
        PageRequest pageable;
        if (SortDirection.interpret(order).name().equalsIgnoreCase("ASCENDING")) {
            pageable = PageRequest.of(pageNumber, size, Sort.by(sortBy).ascending());
        } else {
            pageable = PageRequest.of(pageNumber, size, Sort.by(sortBy).descending());
        }
        return astreRepository.findAll(pageable);
    }

    public Page<Astre> getPaginatedAstres(int pageNumber, int size) {
        return this.getPaginatedAstres(pageNumber, size, "astreID", "asc");
    }

    @Override
    public Optional<Astre> getAstreById(AstreID astreID) {
        return astreRepository.findById(astreID);
    }


    @Override
    public Optional<Astre> saveAstre(Astre astre) {
        AstreID astreID = astre.getAstreID();
        if (astreRepository.findById(astreID).isEmpty()) {
            astre.setDate_added(LocalDate.now());
            astre.setLast_modified(LocalDate.now());
            return Optional.of(astreRepository.save(astre));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Astre> updateAstre(AstreDTO astreDTO) {
        Optional<Astre> optAstre = astreRepository.findById(astreDTO.astreID());
        if (optAstre.isEmpty()) {
            return saveAstre(createOrUpdateTimestamps(convertToEntity(astreDTO)));
        }
        Astre astre = createOrUpdateTimestamps(convertToEntity(astreDTO));
        return Optional.of(astreRepository.save(astre));

    }

    @Override
    public boolean deleteAstre(AstreID astreID) {
        Optional<Astre> potentialAstre = astreRepository.findById(astreID);
        if (potentialAstre.isPresent()) {
            astreRepository.deleteById(astreID);
            log.info(astreID.getType() + "/" + astreID.getSubtype() + "---" + astreID.getName() + "  got deleted");

            // !Never log confidential informations
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            try {
                String jsonString = mapper.writeValueAsString(potentialAstre.get());
                log.debug("Deleted this entry : " + jsonString);
            } catch (Exception e) {
                log.error("Error mapping deleted entry into json", e);
            }
        }
        return true;
    }

    @Override
    public Optional<Astre> upsertAstre(AstreDTO astreDTO) {
        return updateAstre(astreDTO);
    }

    @Override
    public List<Astre> upsertAstres(List<AstreDTO> astresDTO) {
        List<Astre> updatedAstres = new ArrayList<>();
        for (AstreDTO astreDTO : astresDTO) {
            updateAstre(astreDTO).map(updatedAstres::add);
        }
        return updatedAstres;
    }

    /**
     * Will update an Astre's ID, save with the new ID then delete the older ID.
     * Doesn't work if oldID doesn't exist or newID already exist.
     *
     * @param oldID AstreID to be changed
     * @param newID new AstreID to use
     * @return The new Astre created/updated
     */
    @Override
    public Optional<Astre> updateAstreID(AstreID oldID, AstreID newID) {
        Objects.requireNonNull(oldID);
        Objects.requireNonNull(newID, "Newer ID can't be null");

        var astreDB = astreRepository.findById(oldID);
        if (astreDB.isEmpty() || astreRepository.findById(newID).isPresent()) {
            return Optional.empty();
        }
        Astre astreCopy = astreDB.get().clone();
        astreCopy.setAstreID(newID);
        astreCopy.setLast_modified(LocalDate.now());
        Astre newAstre = astreRepository.save(astreCopy);
        astreRepository.deleteById(oldID);
        return Optional.of(newAstre);
    }

    private Astre convertToEntity(AstreDTO astreDTO) {

        Astre astre = new Astre(astreDTO.astreID(),
                astreDTO.subname(), astreDTO.tags(), astreDTO.excluded_tags(), astreDTO.link(), astreDTO.description(), astreDTO.parentAstreID(), astreDTO.parent(), astreDTO.id(),
                LocalDate.now(), LocalDate.now(), astreDTO.fromBefore());

        if (astreDTO.fromBefore() == null) {
            astre.setFrom_before(Boolean.TRUE);
        }
        return astre;
    }

    /**
     * Update timestamp of Astre, if the astre doesn't exist in DB also update the date_added
     *
     * @param astre The Astre to modify
     * @return The astre with updated timestamps
     */
    private Astre createOrUpdateTimestamps(Astre astre) {
        Optional<Astre> astreDB = astreRepository.findById(astre.getAstreID());
        if (astreDB.isPresent()) {
            astre.setDate_added(astreDB.get().getDate_added());
            if (!astre.equals(astreDB.get())) {
                astre.setLast_modified(LocalDate.now());
            }
        } else {
            astre.setDate_added(LocalDate.now());
            astre.setLast_modified(LocalDate.now());
        }
        return astre;
    }
}
