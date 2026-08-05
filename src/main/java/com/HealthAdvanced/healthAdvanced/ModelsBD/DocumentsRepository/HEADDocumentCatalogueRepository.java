package com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository;

import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocumentCatalogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HEADDocumentCatalogueRepository extends JpaRepository<HEADDocumentCatalogue, Integer> {
}