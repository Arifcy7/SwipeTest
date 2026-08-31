# EDGE-GEOINT — Product Requirements Document

**Version:** 1.0
**Status:** Product Definition
**Product:** EDGE-GEOINT
**Domain:** Earth Observation / Geospatial Intelligence
**Deployment Philosophy:** On-Premises / Offline-First
**Primary Architecture:** Software + Edge Hardware + Advanced AI

---

# 1. Executive Summary

EDGE-GEOINT is an offline-first Earth Observation intelligence platform designed to transform large satellite-imagery archives from passive storage into an actively searchable intelligence source.

Traditional satellite imagery systems primarily allow analysts to search using metadata:

* coordinates;
* acquisition date;
* satellite;
* sensor;
* product type;
* geographic area.

EDGE-GEOINT adds a semantic intelligence layer.

An analyst can ask:

> "Show me locations where new structures appeared near a river."

or:

> "Find locations visually similar to this area."

or:

> "What changed in this region during the last six months?"

The platform retrieves relevant imagery, analyses temporal changes, suppresses environmental false alarms, and presents the analyst with evidence, confidence and provenance.

The system is divided into **three product phases**.

---

# 2. Three-Part Product Strategy

```text
                    EDGE-GEOINT
                         |
          +--------------+--------------+
          |              |              |
          v              v              v

       PHASE 1        PHASE 2        PHASE 3

        BASIC         HARDWARE       ADVANCED
         FIND          CARRY        UNDERSTAND

      PS Minimum      Edge Node      AI / USP
      Software       Offline Field   Intelligence
                     Capability
```

---

# 3. Phase 1 — BASIC

## "The Bare Minimum Expected From Us"

This phase exists primarily to satisfy the organiser's problem statement.

It should **not** attempt to solve every possible defence/intelligence problem.

The objective is to build a technically credible EO intelligence platform capable of:

1. ingesting imagery;
2. indexing imagery;
3. searching imagery semantically;
4. searching by image similarity;
5. analysing change over time;
6. suppressing obvious false alarms;
7. discovering similar locations;
8. preserving provenance;
9. supporting analyst review;
10. operating completely offline.

---

## 3.1 Data Ingestion

The platform shall support:

* GeoTIFF;
* Cloud Optimized GeoTIFF;
* organiser-defined compatible formats.

For every scene, the system should preserve:

* source ID;
* acquisition date/time;
* sensor;
* platform;
* CRS;
* geographic bounds;
* resolution;
* bands;
* processing metadata.

Imagery should be converted into searchable tiles while retaining a link to the original scene.

```text
Satellite Scene
      |
      v
Validation
      |
      v
Metadata Extraction
      |
      v
Quality Assessment
      |
      v
Tiling
      |
      v
Embedding Generation
      |
      v
Vector Index
```

---

# 3.2 Semantic Search

The analyst should be able to enter natural-language queries.

Examples:

> "Newly built structures near a river."

> "Large vehicle concentrations on open ground."

> "Dense development around a major road."

The system:

```text
Natural Language
       |
       v
Text Embedding
       |
       v
Vector Search
       |
       v
Metadata Filtering
       |
       v
Ranking
       |
       v
Relevant Imagery
```

---

# 3.3 Image-to-Image Search

An analyst should be able to select an interesting image/tile.

The platform finds visually or semantically similar locations.

```text
Reference Image
      |
      v
Image Embedding
      |
      v
Vector Search
      |
      v
Similar Locations
```

This directly supports discovery without requiring the analyst to formulate another textual query.

---

# 3.4 Multi-Temporal Change Detection

The system shall support:

* appearance;
* disappearance;
* expansion;
* contraction;
* structural development;
* water-extent changes;
* road development;
* clearance.

Example:

```text
2021       2022       2023       2024
 |          |          |          |
 v          v          v          v
No feature  No feature Small      Large
                      change      change
```

The system should determine the earliest usable observation supporting the change.

---

# 3.5 False-Alarm Suppression

This is one of the most important technical components.

The platform must not simply perform:

```text
Image A - Image B = CHANGE
```

because this generates massive numbers of false positives.

The system must account for:

* clouds;
* haze;
* shadows;
* snow;
* season;
* illumination;
* viewing angle;
* sensor differences;
* image registration;
* radiometric differences.

The output should therefore be:

```text
Candidate Change
       |
       v
Quality Assessment
       |
       v
Registration Check
       |
       v
Environmental Analysis
       |
       v
Confidence
       |
       v
Analyst Review
```

---

# 3.6 Similar-Site Discovery

If an analyst discovers an interesting location, the system should answer:

> "Where else does something similar exist?"

The platform can use embeddings and clustering to identify groups of similar locations.

---

# 3.7 Analyst Workbench

The analyst should see:

* map;
* imagery;
* search results;
* acquisition dates;
* before/after imagery;
* change overlays;
* confidence;
* sensor information;
* processing history;
* provenance.

The analyst can:

* confirm;
* reject;
* investigate;
* bookmark;
* export.

---

# 3.8 Incremental Ingestion

New imagery must not require rebuilding the entire archive.

```text
Existing Archive
       |
       v
Existing Index

New Scene
       |
       v
Process New Scene
       |
       v
Generate Embedding
       |
       v
Insert Into Index
```

---

# 3.9 Offline Operation

The system must continue working when:

```text
Internet = OFF
```

All required:

* models;
* weights;
* libraries;
* datasets;
* indexes;

must already be staged locally.

---

# 3.10 Phase 1 Success Criteria

Phase 1 is complete when the evaluator can:

* ingest EO imagery;
* search using natural language;
* perform image-to-image search;
* filter by location/time/sensor;
* analyse changes;
* suppress obvious false alarms;
* identify earliest supporting evidence;
* discover similar locations;
* review results;
* preserve provenance;
* incrementally ingest new data;
* operate offline.

**This is the competition baseline.**

---

# 4. Phase 2 — HARDWARE

# "CARRY — Take Intelligence to the Edge"

The second phase transforms EDGE-GEOINT from a server-only platform into an **edge intelligence system**.

The important distinction is:

> We are not trying to build another smartphone.

We are building a **purpose-built, controlled, offline intelligence terminal**.

---

# 4.1 Why Hardware?

A central server is excellent when connectivity exists.

However, there are environments where connectivity may be:

* unavailable;
* unreliable;
* deliberately disabled;
* too slow;
* operationally undesirable.

Therefore:

```text
CENTRAL SYSTEM
      |
      | Mission Package
      v
EDGE DEVICE
      |
      v
Offline Intelligence
```

---

# 4.2 Mission Package

The central platform creates a mission-specific package.

It can contain:

* relevant satellite imagery;
* imagery embeddings;
* vector index;
* geographic data;
* offline maps;
* models;
* metadata;
* previous analysis;
* provenance.

The edge device receives only what is required.

---

# 4.3 Edge Device

The hardware should be designed around the workload rather than around consumer electronics.

Potential architecture:

```text
+--------------------------------+
|        EDGE-GEOINT NODE        |
|                                |
|  Display                        |
|  Local UI                       |
|                                |
|  AI Accelerator                 |
|  CPU                            |
|  RAM                            |
|  NVMe Storage                   |
|                                |
|  GNSS                           |
|  Secure Storage                 |
|                                |
|  Battery                        |
+--------------------------------+
```

---

# 4.4 Form Factor

The initial prototype should use commercially available compute hardware.

Possible form factors:

### Prototype

Rugged tablet / compact computer.

### Field Prototype

Rugged handheld.

### Advanced Prototype

Forearm-mounted terminal.

### Future

Wearable edge-computing platform.

The wearable form factor is a **product differentiator**, not a prerequisite for PS compliance.

---

# 4.5 Offline Intelligence

The device should be able to perform selected operations without network access.

For example:

```text
Satellite Imagery
       |
       v
Local Search
       |
       v
Relevant Locations
       |
       v
Local Temporal Analysis
       |
       v
Evidence
```

The device does not need to run the largest foundation model.

Instead:

```text
SERVER

Heavy Models
Large Archive
Complex Processing
        |
        v
Mission Package
        |
        v

EDGE

Lightweight Models
Relevant Imagery
Local Index
Fast Inference
```

This is a critical architecture decision.

---

# 4.6 Secure-by-Design Hardware

The platform should not claim:

> "The device cannot be hacked."

Instead, it should claim:

> "The device is purpose-built with a significantly reduced attack surface compared with a general-purpose computing platform."

Potential features:

* minimal operating environment;
* encrypted storage;
* secure boot;
* signed software;
* signed model packages;
* disabled unnecessary services;
* controlled updates;
* audit logs;
* local authentication.

---

# 4.7 Synchronisation

When connectivity becomes available:

```text
EDGE
 |
 | New observations
 | Analyst decisions
 | Local analysis
 v
CENTRAL
```

The system preserves the origin of every observation.

---

# 4.8 Hardware Success Criteria

Phase 2 is complete when:

* a Mission Package can be generated;
* the package can be transferred to the edge device;
* the device operates without network access;
* local search works;
* selected AI workloads work locally;
* analyst observations can be stored;
* the observations can later be synchronised;
* provenance is preserved.

---

# 5. Phase 3 — ADVANCED

# "UNDERSTAND — The Phenomenal USP"

This phase should differentiate EDGE-GEOINT from a conventional satellite-image search platform.

The objective is to move from:

> **"Search the imagery."**

to:

> **"Continuously build intelligence from the imagery."**

---

# 5.1 Persistent Earth Intelligence

Instead of treating every query independently, the platform builds a temporal understanding of locations.

```text
             LOCATION
                 |
     +-----------+-----------+
     |           |           |
   2021        2023        2026
     |           |           |
     +-----------+-----------+
                 |
          Intelligence
            Timeline
```

The platform understands:

* what existed;
* what appeared;
* what disappeared;
* what expanded;
* what contracted;
* when it changed;
* whether the change persisted.

---

# 5.2 "Tell Me What Changed"

Instead of requiring:

```text
AOI
+
Date A
+
Date B
```

the analyst can ask:

> "Tell me what changed here over the last three years."

The system automatically:

1. retrieves historical observations;
2. evaluates image quality;
3. aligns observations;
4. detects candidate changes;
5. suppresses environmental effects;
6. constructs a timeline;
7. ranks significant changes;
8. presents evidence.

---

# 5.3 Earliest Evidence Engine

A particularly powerful capability is:

> **When did this change actually begin?**

Instead of only saying:

> "Construction detected."

the system produces:

```text
2021 — No evidence
2022 — No evidence
2023 — Possible activity
2024 — Strong evidence
2025 — Confirmed expansion
```

This converts change detection into **temporal reasoning**.

---

# 5.4 Anomaly Intelligence

The system learns what is normal for a location.

```text
Historical Data
       |
       v
Normal Baseline
       |
       v
New Observation
       |
       v
Deviation
       |
       v
Anomaly Candidate
```

The output is not:

> "This is definitely suspicious."

Instead:

> "This observation deviates significantly from the historical pattern and requires analyst review."

This distinction is essential.

---

# 5.5 Self-Expanding Discovery

This is one of the strongest potential USPs.

Suppose an analyst identifies one interesting location.

The system automatically asks:

> "What other locations in the archive exhibit similar characteristics?"

It can combine:

* visual similarity;
* semantic similarity;
* temporal behaviour;
* geographic context.

```text
                  Reference Site
                       |
          +------------+------------+
          |            |            |
      Visual        Semantic      Temporal
     Similarity    Similarity     Behaviour
          |            |            |
          +------------+------------+
                       |
                       v
               Related Locations
```

The analyst doesn't need to manually search every location.

---

# 5.6 Multimodal Intelligence Queries

The advanced system should eventually allow:

```text
TEXT
IMAGE
LOCATION
TIME
METADATA
CHANGE HISTORY
```

to participate in one query.

Example:

> "Find locations visually similar to this image where significant structural development occurred during the selected period."

This is much more powerful than conventional keyword search.

---

# 5.7 Analyst-in-the-Loop Intelligence

The analyst becomes part of the system.

If an analyst repeatedly confirms or rejects certain results, the system can learn relevance patterns.

```text
AI Result
    |
    v
Analyst
    |
 +--+--+
 |     |
YES    NO
 |     |
 +--+--+
    |
    v
Feedback
    |
    v
Better Ranking
```

Possible mechanisms:

* relevance feedback;
* reranking;
* hard-negative mining;
* confidence calibration;
* query refinement.

---

# 5.8 Intelligence Graph

A future version can connect observations into an intelligence graph.

```text
                 LOCATION
                    |
          +---------+---------+
          |         |         |
       Imagery    Change    Sensor
          |         |         |
          +----+----+----+----+
               |
          Temporal Event
               |
       Similar Locations
               |
          Analyst Evidence
```

This enables questions such as:

> "Show me locations exhibiting the same change pattern."

rather than simply:

> "Show me similar images."

---

# 5.9 Edge + Central Intelligence

The final architecture becomes:

```text
                    CENTRAL
                       |
          +------------+------------+
          |            |            |
        Archive      Models      Intelligence
          |            |            |
          +------------+------------+
                       |
                 Mission Package
                       |
             +---------+---------+
             |         |         |
           EDGE A    EDGE B    EDGE C
             |         |         |
             +---------+---------+
                       |
                  Synchronisation
                       |
                    CENTRAL
```

Each edge node can operate independently.

The central system learns from approved observations returned from the field.

---

# 5.10 Ultimate Product Vision

The final system should evolve from:

### Version 1

> "Search my satellite imagery."

to:

### Version 2

> "Take this intelligence with me."

to:

### Version 3

> "Tell me what changed, what is unusual, and where else I should look."

That progression is the core product strategy.

---

# 6. Product Differentiation

| Capability                       | Conventional EO Catalogue | EDGE-GEOINT     |
| -------------------------------- | ------------------------- | --------------- |
| Metadata search                  | Yes                       | Yes             |
| Text semantic search             | Limited/No                | Yes             |
| Image similarity                 | Limited                   | Yes             |
| Change detection                 | Some systems              | Yes             |
| False-alarm suppression          | Variable                  | Core capability |
| Earliest change                  | Limited                   | Yes             |
| Similar-site discovery           | Limited                   | Yes             |
| Analyst feedback                 | Variable                  | Yes             |
| Offline operation                | Rare                      | Core            |
| Edge deployment                  | Rare                      | Core            |
| Mission packages                 | Rare                      | Yes             |
| Persistent temporal intelligence | Limited                   | Advanced USP    |
| Multimodal reasoning             | Emerging                  | Advanced USP    |
| Anomaly intelligence             | Emerging                  | Advanced USP    |
| Distributed edge intelligence    | Rare                      | Future USP      |

---

# 7. Product Positioning

The recommended positioning is:

> **EDGE-GEOINT is an offline-first Earth-observation intelligence platform that helps analysts FIND relevant locations, CARRY mission-specific intelligence to disconnected environments, and UNDERSTAND how locations and patterns evolve over time.**

The product should **not** be positioned simply as:

* a satellite image viewer;
* an AI chatbot for maps;
* a military target detector;
* a rugged smartphone.

The core asset is the **intelligence architecture**.

---

# 8. Product Philosophy

Three principles govern the product:

### FIND

Search the world's imagery by meaning.

### CARRY

Bring intelligence to disconnected environments.

### UNDERSTAND

Turn observations into persistent temporal intelligence.

```text
             FIND
              |
              v
            CARRY
              |
              v
          UNDERSTAND
              |
              v
       Better Intelligence
              |
              +----> FIND
```

This creates a continuous intelligence loop rather than a one-time search application.
