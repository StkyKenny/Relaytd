package me.stky.relaytd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import me.stky.relaytd.api.model.AstreID;
import me.stky.relaytd.api.model.CollectionEntry;
import me.stky.relaytd.api.model.UpdateAstreIDRequest;
import me.stky.relaytd.api.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "BearerAuthentication")
@RequestMapping("/api/collections")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @Operation(summary = "", description = "")
    @PostMapping("/getCollectionFromName")
    public ResponseEntity<List<CollectionEntry>> getCollectionFromName(@RequestBody String collection) {
        return ResponseEntity.ok(collectionService.getCollectionFromName(collection));
    }


    @Operation(summary = "", description = "")
    @PostMapping("/getCollection")
    public ResponseEntity<List<CollectionEntry>> getCollection(@Valid @RequestBody AstreID astreID) {
        return ResponseEntity.ok(collectionService.getFromSource(astreID));
    }


    @Operation(summary = "", description = "")
    @PostMapping("/newEntry")
    public ResponseEntity<CollectionEntry> saveCollectionEntry(@Valid @RequestBody CollectionEntry entry) {
        return collectionService.saveCollectionEntry(entry, entry.getAstreID())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }


    @Operation(summary = "", description = "")
    @PutMapping("/changeAstreSource")
    public ResponseEntity<List<CollectionEntry>> changeCollectionSource(@Valid @RequestBody UpdateAstreIDRequest updateAstreIDRequest) {
        return ResponseEntity.ok(collectionService.getFromSource(updateAstreIDRequest.getNewID()));
    }

    //@PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "", description = "")
    @GetMapping("/getAll")
    public ResponseEntity<List<CollectionEntry>> getAll() {
        return ResponseEntity.ok(collectionService.getAll());
    }
}
