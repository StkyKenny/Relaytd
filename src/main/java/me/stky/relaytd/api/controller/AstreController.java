package me.stky.relaytd.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import me.stky.relaytd.api.model.*;
import me.stky.relaytd.api.service.AstreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@RestController
@SecurityRequirement(name = "BearerAuthentication")
@RequestMapping("/api/astres")
public class AstreController {

    @Autowired
    AstreService astreService;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping(path = "/welcome")
    public ResponseEntity<String> getWelcome(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                System.out.println(" cookie: " + cookie.getName() + " = " + cookie.getValue());
            }
        }
        System.out.println();
        System.out.println();
        return new ResponseEntity<>("Welcome to the controller", HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Save an astre", description = "Save an astre, doesn't save if the ID is already used")
    @PostMapping("/astre")
    public ResponseEntity<Astre> saveAstre(@Valid @RequestBody AstreDTO astreDTO) {
        return astreService.upsertAstre(astreDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Get all astres", description = "Get all astres")
    @GetMapping("/getall")
    public ResponseEntity<List<Astre>> getAstres() {
        return ResponseEntity.ok(astreService.getAllAstre());
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Get all astres in a paginated format", description = "Get all astres")
    @GetMapping("/getallpaginated")
    public ResponseEntity<Page<Astre>> getPaginatedAstres(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "1000") int size,
            @RequestParam(defaultValue = "astreID") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        return ResponseEntity.ok(astreService.getPaginatedAstres(pageNumber, size, sortBy, order));
    }


    @Operation(summary = "Get an astre", description = "Get an astre using a type and name")
    @GetMapping("/astre")
    public ResponseEntity<Astre> getAstre(String type, String subtype, String name) {
        // Don't use Request Body on Get Mapping, it is allowed but most of the time not supported
        AstreID astreID = new AstreID(type, subtype, name);
        return astreService.getAstreById(astreID)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @Operation(summary = "Get all Astres and a list mapping all astreID to their children", description = "")
    @GetMapping("/astreslinks")
    public ResponseEntity<AstreLinksResponse> getAstresAndLinks() {

        List<Astre> astres = astreService.getAllAstre();
        ObjectMapper objectMapper = new ObjectMapper();

        ConcurrentMap<AstreID, List<Astre>> childrenMap = astres.parallelStream()
                .filter(astre -> astre.getParentAstreID() != null)
                .filter(astre -> Objects.equals(astre.getAstreID().getType(), "topic"))
                .collect(Collectors.groupingByConcurrent(Astre::getParentAstreID));


        List<AstreChildren> childrenList = childrenMap.entrySet().parallelStream().map(entry -> new AstreChildren(entry.getKey(), entry.getValue().stream().map(Astre::getAstreID).toList())).toList();
        return ResponseEntity.ok(new AstreLinksResponse(astres, childrenList));

    }
    /*
    // This method is kept for archiving purpose : Using Path Variable

    @Operation(summary = "Get an astre", description = "Get an astre using a type and name")
    @GetMapping("/{type}/{name}")
    public ResponseEntity<Astre> getAstre(@PathVariable("type") String type, @PathVariable("name") String name) {
        return astreService.getAstreById(type, name)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }*/

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Update an astre", description = "Update (or create) an astre using a type and name")
    @PutMapping("/astre")
    public ResponseEntity<Astre> update(@Valid @RequestBody AstreDTO astreDTO) {
        return astreService.updateAstre(astreDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Delete an astre", description = "Delete using the type and name")
    @DeleteMapping("/astre")
    public ResponseEntity<Object> deleteAstre(@Valid @RequestBody AstreID astreID) {
        if (astreService.deleteAstre(astreID)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Save/Update multiples astre", description = "Mass update")
    @PostMapping("/astres")
    public ResponseEntity<List<Astre>> upsertAstres(@Valid @RequestBody List<AstreDTO> astresDTO) {
        System.out.println("save multiples");
        List<Astre> upsertedAstres = astreService.upsertAstres(astresDTO);
        if (upsertedAstres.isEmpty()) {
            return ResponseEntity.noContent().build(); // returns 204 No Content with no body
        }
        return ResponseEntity.ok(upsertedAstres); // returns 200 regardless of list content
    }


    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Update an Astre's ID", description = "Update an Astre's ID, remove the old one")
    @PutMapping("astreid")
    public ResponseEntity<Astre> updateAstreID(@Valid @RequestBody UpdateAstreIDRequest updateRequest) {
        return astreService.updateAstreID(updateRequest.getOldID(), updateRequest.getNewID())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}
