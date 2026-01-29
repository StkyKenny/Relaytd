package me.stky.relaytd.services;


import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreID;
import me.stky.relaytd.api.repository.AstreRepository;
import me.stky.relaytd.api.service.AstreServiceImpl;
import me.stky.relaytd.config.SpringDataConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AstreServiceTest {

    @InjectMocks
    private AstreServiceImpl astreService;

    @Mock
    private AstreRepository astreRepository;

    @BeforeEach
    void init() {
        astreService = new AstreServiceImpl(astreRepository);
    }

    @Test
    void testSaveAstre_AstreAlreadyPresentInDB() {
        int number = 1;
        Astre AstreInDB = createAstre(number);
        Astre newAstre = createAstre(number);

        AstreID astreID = newAstre.getAstreID();
        when(astreRepository.findById(astreID)).thenReturn(Optional.of(AstreInDB));

        var result = astreService.saveAstre(newAstre);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void testSaveAstre_NewAstre() {
        int number = 1;
        Astre astre = createAstre(number);
        Optional<Astre> expectedAstre = Optional.of(createAstre(number));

        AstreID astreID = astre.getAstreID();
        when(astreRepository.findById(astreID)).thenReturn(Optional.empty());
        when(astreRepository.save(astre)).thenReturn(astre);

        var result = astreService.saveAstre(astre);
        assertTrue(result.isPresent());
        assertEquals(expectedAstre.get(), result.get());
    }

    @Test
    void testUpdateAstre_Nullcheck1() {
        Astre astre = createAstre(1);
        AstreID newID = astre.getAstreID();

        assertThrows(NullPointerException.class, () -> astreService.updateAstreID(null, newID));
    }

    @Test
    void testUpdateAstre_Nullcheck2() {
        Astre astre = createAstre(1);
        AstreID oldID = astre.getAstreID();

        assertThrows(NullPointerException.class, () -> astreService.updateAstreID(oldID, null));
    }

    @Test
    void testUpdateAstre_notFound() {
        Astre astre = createAstre(1);
        AstreID oldID = astre.getAstreID();
        AstreID newID = createAstreID(2);


        when(astreRepository.findById(oldID)).thenReturn(Optional.empty());
        Optional<Astre> result = astreService.updateAstreID(oldID, newID);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testUpdateAstre_newIDAlreadyUsed() {
        Astre astre = createAstre(1);
        AstreID oldID = astre.getAstreID();
        Astre astreDB = createAstre(2);
        AstreID newID = astreDB.getAstreID();


        when(astreRepository.findById(oldID)).thenReturn(Optional.of(astre));
        when(astreRepository.findById(newID)).thenReturn(Optional.of(astreDB));
        Optional<Astre> result = astreService.updateAstreID(oldID, newID);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testUpdateAstre() {
        Astre astre = createAstre(1);
        AstreID oldID = astre.getAstreID();
        Astre astreDB = createAstre(2);
        AstreID newID = astreDB.getAstreID();
        Astre astreCopy = astreDB.clone();


        lenient().when(astreRepository.findById(oldID)).thenReturn(Optional.of(astre)); // lenient here to ignore stubbing warning and not make it global
        lenient().when(astreRepository.findById(newID)).thenReturn(Optional.empty());
        when(astreRepository.save(astreCopy)).thenReturn(astreCopy);


        Optional<Astre> result = astreService.updateAstreID(oldID, newID);
        assertEquals(Optional.of(astreDB), result);

    }

    @Test
    void testDeleteAstre() {
        String type = "type1";
        String subtype = "name1";
        String name = "name1";
        AstreID astreID = new AstreID(type, subtype, name);
        when(astreRepository.findById(astreID)).thenReturn(Optional.empty());

        var result = astreService.deleteAstre(astreID);
        assertTrue(result);
    }

    @Test
    void testGetAstres_tooMany() {

        int astreNumber = SpringDataConfig.MAX_UNPAGED_ROWS + 1;
        when(astreRepository.count()).thenReturn((long) astreNumber);
        assertThrows(ResponseStatusException.class, () -> astreService.getAllAstre());
    }

    @Test
    void testGetPaginatedAstres_empty() {

        int pageNumber = 0;
        int size = 10;

        Page<Astre> fakePage = new PageImpl<>(List.of());
        when(astreRepository.findAll(any(Pageable.class))).thenReturn(fakePage);

        Page<Astre> result = astreService.getPaginatedAstres(pageNumber, size);

        assertTrue(result.isEmpty());
        assertEquals(1, result.getTotalPages());
        assertEquals(List.of(), result.getContent());
    }

    @Test
    void testGetPaginatedAstres_ValidPage() {

        int pageNumber = 0;
        int size = 10;
        int astreNumber = 101;
        String sortBy = "astreID";

        PageRequest pageable = PageRequest.of(pageNumber, size, Sort.by(sortBy).ascending());

        List<Astre> bulkAstres = createBulkAstres(astreNumber);
        Page<Astre> fakePage = new PageImpl<>(bulkAstres, pageable, astreNumber);
        when(astreRepository.findAll(any(Pageable.class))).thenReturn(fakePage);

        Page<Astre> result = astreService.getPaginatedAstres(pageNumber, size);

        assertFalse(result.isEmpty());
        assertEquals(Math.ceilDiv(astreNumber, size), result.getTotalPages());
        assertEquals(bulkAstres, result.getContent());
    }

    @Test
    void testGetPaginatedAstres_integrity() {

        int pageNumber = 0;
        int size = 10;
        int astreNumber = 101;
        String sortBy = "astreID";

        PageRequest pageable = PageRequest.of(pageNumber, size, Sort.by(sortBy).ascending());

        List<Astre> bulkAstres = createBulkAstres(astreNumber);
        Page<Astre> fakePage = new PageImpl<>(bulkAstres, pageable, astreNumber);
        when(astreRepository.findAll(any(Pageable.class))).thenReturn(fakePage);

        Page<Astre> result = astreService.getPaginatedAstres(pageNumber, size);

        assertFalse(result.isEmpty());
        assertEquals(Math.ceilDiv(astreNumber, size), result.getTotalPages());
        assertEquals(bulkAstres, result.getContent());

        List<Astre> resultList = result.toList();
        for (int i = 0; i < resultList.size(); i++) {
            assertEquals(createAstre(i), resultList.get(i));
        }
    }


    private Astre createAstre(int number) {
        return new Astre(createAstreID(number), "subname",
                "tag1,tag2", "excludedtags", "link", "description",
                null, "no-parent", "id",
                LocalDate.now(), LocalDate.now(), Boolean.FALSE);
    }

    private AstreID createAstreID(int number) {
        return new AstreID("type" + number, "subtype" + number, "name" + number);
    }

    private List<Astre> createBulkAstres(int total) {
        List<Astre> bulkAstres = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            bulkAstres.add(createAstre(i));
        }
        return bulkAstres;
    }
}
