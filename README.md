# PRD — EDGE-GEOINT
## Semantic Retrieval and Multi-Temporal Change Analysis of Satellite Imagery

**Version:** 1.0  
**Status:** Proposed / SIH Prototype  
**Domain:** Defence GEOINT / Earth Observation  
**Deployment:** On-premises, offline-capable  
**Primary data:** Sentinel-2 Level-2A, multi-temporal multispectral GeoTIFF/COG  
**Primary geography:** India

---

## 1. Product Overview

EDGE-GEOINT is an on-premises geospatial intelligence platform that turns a large archive of multi-temporal satellite imagery into a searchable, change-aware intelligence system.

The analyst can:
- search imagery using natural language;
- search by an example image/location and find similar sites;
- combine semantic search with spatial, temporal and sensor filters;
- analyse a location across multiple observations;
- identify supported changes such as construction, clearance, road development and water-extent variation;
- estimate the earliest usable observation supporting a change;
- suppress pseudo-changes caused by clouds, haze, shadows, snow, seasonality, illumination, viewing geometry, radiometric inconsistency and imperfect co-registration;
- receive ranked candidates with confidence, evidence and provenance;
- validate/reject candidates and provide feedback;
- incrementally ingest new imagery without rebuilding the complete index;
- operate locally after models, libraries and datasets are staged.

This is aligned with the SIH problem statement.

---

## 2. Problem Statement

Conventional satellite-imagery workflows are primarily metadata-driven. Analysts generally search using coordinates, acquisition date, platform/sensor, product type or area of interest before inspecting imagery.

This creates:
1. **Discovery problem:** analysts may need prior knowledge of where and when to look.
2. **Semantic problem:** large archives are difficult to search by actual imagery content or to mine for similar locations.
3. **Temporal reliability problem:** image differences may represent acquisition conditions rather than genuine ground change.

The product shifts the workflow from:

> Find imagery → manually inspect → manually compare → interpret

to:

> Describe requirement → retrieve candidates → collect temporal evidence → infer semantic change → suppress pseudo-change → rank evidence → analyst validates.

---

## 3. Product Goals

### G1 — Semantic Retrieval
Support natural-language search such as:
- “newly constructed structures near a river”
- “large vehicle concentrations on open ground”
- “industrial facilities near major roads”

### G2 — Image-to-Image Retrieval
Allow an analyst to select an interesting region and discover similar sites.

### G3 — Multi-Temporal Change Analysis
Analyse a selected area across a requested time window.

### G4 — Semantic Change Understanding
Infer meaningful region-level change rather than treating pixel difference as proof.

### G5 — False-Alarm Suppression
Handle seasonality, illumination, clouds, haze, snow, shadows, sensor/view-angle differences, radiometric inconsistency and registration error.

### G6 — Evidence-Based Confidence
Combine multiple evidence signals into a confidence/uncertainty estimate.

### G7 — Analyst-Centric Review
Show before/after evidence, timeline, location, source, confidence and processing provenance.

### G8 — Scalable Offline Operation
Support local vector indexing, incremental ingestion and no runtime cloud/API dependency during evaluation.

---

## 4. Non-Goals

The MVP will not:
- claim perfect automated intelligence interpretation;
- treat every pixel difference as a real-world change;
- replace analyst judgement;
- train a new EO foundation model from scratch;
- detect arbitrary unsupported change categories;
- depend on external inference APIs;
- claim pretrained models are automatically optimized for Indian terrain without validation.

---

## 5. Design Principles

### P1 — Evidence over single-pixel classification
A pixel can represent multiple materials/objects. Decisions must use region, spectral, spatial and temporal evidence.

### P2 — Semantic change over raw difference
Target:

> semantic state at T1 → semantic state at T2 → supported transition

rather than:

> pixel(T1) != pixel(T2) → change.

### P3 — Pretrained foundations + task-specific intelligence
Use pretrained EO/VLM models for general representations; develop the temporal reasoning, evidence fusion and uncertainty components specific to this problem.

### P4 — Analyst in the loop
AI identifies and ranks candidates; the analyst validates important detections.

### P5 — Provenance first
Every result must be traceable to source scenes and processing/model versions.

### P6 — Offline first
Core evaluation functionality must work locally after staging.

---

## 6. Data Strategy

### 6.1 Primary dataset

**Sentinel-2 Level-2A** for selected Indian AOIs over approximately 5–10 years where coverage is usable.

Do not attempt to download all of India. Build a representative research archive.

### 6.2 AOI diversity

Recommended initial AOIs:
- urban/industrial;
- agricultural;
- arid/desert;
- mountainous;
- coastal/riverine.

### 6.3 Source format

Retain original source products for provenance and create standardized GeoTIFF/COG analysis products.

### 6.4 Prithvi-compatible six-band representation

For the documented Prithvi-EO-2.0 HLS configuration, the six channels are:

- B02 — Blue
- B03 — Green
- B04 — Red
- B05 — NIR narrow
- B06 — SWIR 1
- B07 — SWIR 2

Therefore, use these corresponding Sentinel-2 bands for the Prithvi-compatible product.

Do not silently substitute B08/B11/B12 while claiming exact Prithvi six-band compatibility.

Additional Sentinel-2 bands may be retained for auxiliary experiments.

### 6.5 Resolution

Preserve source resolution. Resample model inputs to a documented common grid.

Because the pretrained Prithvi model was trained using HLS at 30 m granularity, direct pretrained-model experiments should use a compatible 30 m analysis grid unless an adaptation experiment explicitly changes this.

All resampling must be recorded in provenance.

### 6.6 Quality information

Retain/use:
- Scene Classification Layer (SCL);
- cloud information where available;
- snow/ice information where available;
- acquisition date/time;
- sensor/platform;
- CRS;
- geotransform;
- resolution;
- source/product identifier.

---

## 7. High-Level Architecture

```text
                 SENTINEL-2 L2A ARCHIVE
                          |
                          v
                 GeoTIFF/COG Ingestion
                          |
                          v
             Geospatial + Quality Processing
                          |
             +------------+-------------+
             |                          |
             v                          v
       RGB Representation          6-Band EO Data
             |                          |
             v                          v
        RemoteCLIP                 Prithvi-EO-2.0
      Semantic Encoder            300M-TL / baseline
             |                          |
             v                          v
      Semantic Embeddings          EO Features
             |                          |
             +------------+-------------+
                          |
                          v
                  Local Vector Index
                          |
                          v
                Candidate Retrieval
                          |
                          v
              Multi-Temporal Evidence
                          |
                          v
             OUR SEMANTIC TEMPORAL MODEL
                          |
                          v
             Change Type + Localization
                          |
                          v
                OUR EVIDENCE FUSION
                          |
                          v
              Confidence / Uncertainty
                          |
                          v
                 Analyst Review Queue
                          |
                          v
                 Feedback / Reranking
```

---

## 8. Model Stack

| Component | Recommended approach | Purpose | Build from scratch? |
|---|---|---|---|
| Semantic retrieval | RemoteCLIP + optional lightweight adaptation | Text↔image and image↔image retrieval | No |
| EO representation | Prithvi-EO-2.0-300M-TL | Multispectral/temporal features | No |
| Region understanding | Foundation features + lightweight head | Semantic regions/states | Head only |
| Temporal change model | Custom temporal head | Semantic transitions and change classes | Yes |
| False-change suppression | Custom evidence fusion | Reject pseudo-change | Yes |
| Confidence | Custom uncertainty/calibration layer | Trust estimation | Yes |
| Similar-site search | FAISS/Qdrant + clustering | Similar locations | No neural model needed |
| Analyst reranking | Lightweight learning-to-rank | Use analyst feedback | Small custom model |

---

## 9. RemoteCLIP

### Role
Semantic image-text retrieval and image-to-image retrieval.

### Input
RGB derived from Sentinel-2:
- Red = B04
- Green = B03
- Blue = B02

### Output
Normalized embeddings.

### Workflow

```text
Natural-language query
        ↓
RemoteCLIP text encoder
        ↓
Query embedding
        ↓
Local vector search
        ↓
Top-K candidate tiles
```

For image search:

```text
Selected image/region
        ↓
RemoteCLIP image encoder
        ↓
Image embedding
        ↓
Vector search
        ↓
Similar locations
```

### Adaptation
Benchmark zero-shot RemoteCLIP on Indian imagery first. If a domain gap is observed, use parameter-efficient/lightweight adaptation rather than retraining from scratch.

---

## 10. Prithvi-EO-2.0

### Recommended starting model
**Prithvi-EO-2.0-300M-TL**

### Role
Multispectral Earth-observation representation and temporal feature extraction.

The TL architecture uses spatiotemporal inputs and incorporates temporal/location information.

### Input
Chronological standardized six-band GeoTIFF sequence:

```text
T1.tif
T2.tif
T3.tif
T4.tif
```

### Output
EO latent features for downstream temporal reasoning.

### Why pretrained?
Training an EO foundation model from scratch is unnecessary and outside practical MVP scope.

### Limitation
Its pretrained representation must be validated on Indian Sentinel-2 data.

---

## 11. Custom Semantic Temporal Model

This is the main task-specific model.

### Purpose
Infer semantic state transitions and meaningful change types.

Example:

```text
2022 → Open land
2023 → Open land
2024 → Construction activity
2025 → Permanent structure
2026 → Permanent structure
```

Output:

```text
Change type: New construction
Earliest supporting observation: 2024
```

### Recommended design
Start with a lightweight temporal transformer/temporal-attention module over per-date EO features.

Input:

```text
F1, F2, F3, ... Fn
```

Output:
- semantic state representation;
- change localization;
- change type;
- temporal transition score.

Initial classes:
- construction;
- clearance;
- road development;
- water-extent variation;
- expansion;
- contraction.

---

## 12. Evidence Fusion / False-Alarm Suppression

This is a core custom component.

### Inputs
- semantic change score;
- temporal consistency;
- spectral consistency;
- spatial consistency;
- cloud/quality score;
- shadow/snow indicators;
- registration quality;
- seasonal/illumination indicators;
- cross-date agreement;
- optional cross-sensor agreement.

### Concept

```text
Semantic evidence --------Temporal evidence ---------Spectral evidence ----------> Evidence Fusion
Quality evidence ----------/
Registration evidence -----/
                              |
                              v
                    Genuine Change Score
```

A candidate should become high confidence only when multiple evidence sources support it.

---

## 13. Confidence / Uncertainty

The system must express uncertainty instead of forcing binary decisions.

Example:

```text
Candidate: New structure

Semantic evidence:       Strong
Temporal persistence:    Strong
Image quality:           High
Registration quality:    Good
Spectral consistency:    Strong

Overall confidence:      HIGH
Earliest evidence:       June 2024
```

If evidence conflicts:

```text
Status: Ambiguous
Reason: insufficient evidence
```

Confidence must be calibrated and evaluated on held-out data.

---

## 14. Retrieval and Indexing

### Vector index
Use a locally deployable solution:
- FAISS for a lightweight prototype; or
- Qdrant for a fuller local service.

Selection criteria:
- offline operation;
- metadata filtering;
- incremental insertion;
- latency;
- memory/storage footprint.

### Indexed record

```text
embedding_id
scene_id
tile_id
AOI_id
acquisition_date
sensor
CRS
bounding_box
resolution
source_path
quality_score
cloud_score
model_name
model_version
preprocessing_version
embedding_version
```

### Search
Semantic:
```text
Text → embedding → vector search → metadata filters → candidates
```

Image:
```text
Image → embedding → vector search → similar locations
```

---

## 15. Multi-Temporal Pipeline

For each candidate:

1. retrieve usable observations in the requested time window;
2. sort chronologically;
3. apply quality masks;
4. co-register observations;
5. normalize as required;
6. generate EO features;
7. infer semantic states;
8. detect transitions;
9. classify supported change;
10. estimate earliest supporting observation;
11. calculate confidence;
12. present evidence to analyst.

Raw image subtraction may be shown as diagnostic evidence but must not be the sole decision rule.

---

## 16. Geospatial Preprocessing

### Validation
- CRS;
- dimensions;
- band order;
- nodata;
- acquisition date;
- reflectance/scaling metadata.

### Reprojection
Use a consistent analysis grid.

### Co-registration
Align temporal observations and record transformation/residual error.

### Resampling
Standardize required bands to the target model grid.

### Quality masking
Mask invalid/cloud/shadow/snow/unusable pixels as appropriate.

### Normalization
Apply model-required scaling/normalization.

### Tiling
Create manageable overlapping tiles and retain geospatial transforms.

---

## 17. Analyst Interface

Each candidate should show:

### Candidate summary
- location;
- query relevance;
- change type;
- confidence;
- earliest supporting date;
- latest observation;
- number of usable observations.

### Evidence
- before;
- intermediate dates;
- after;
- change visualization;
- optional spectral evidence;
- quality mask;
- timeline.

### Provenance
- source scene IDs;
- acquisition dates;
- sensor;
- CRS;
- preprocessing version;
- model version;
- embedding/index version.

### Actions
- Confirm;
- Reject;
- Mark uncertain;
- Add note;
- Export evidence package.

---

## 18. Analyst Feedback

```text
Query
 ↓
Initial retrieval
 ↓
Analyst labels:
Relevant / Irrelevant / Uncertain
 ↓
Reranker
 ↓
Improved ranking
```

Feedback should not silently modify the foundation model and must remain auditable.

---

## 19. Incremental Ingestion

```text
New GeoTIFF/COG
      ↓
Validate
      ↓
Metadata extraction
      ↓
Quality assessment
      ↓
Preprocess / tile
      ↓
+----------------------+
|                      |
v                      v
RemoteCLIP embedding  Prithvi features
|                      |
+----------+-----------+
           ↓
      Update indexes
           ↓
 Available for search
```

No complete index rebuild should be required for every new acquisition.

---

## 20. Data/Storage Layout

```text
/data
  /raw/sentinel2/AOI_01
  /raw/sentinel2/AOI_02

  /analysis_ready/AOI_01/YYYY
  /tiles/AOI_01

  /metadata
    scenes.parquet
    tiles.parquet

  /embeddings
    /remoteclip
    /prithvi

  /indexes
    /remoteclip
    /prithvi

  /models
    /remoteclip
    /prithvi
    /custom

  /results
  /provenance
  /feedback
```

---

## 21. Technology Stack

### Data
- Sentinel-2 L2A
- GeoTIFF / COG
- GDAL
- Rasterio
- STAC-compatible metadata where available

### ML
- PyTorch
- RemoteCLIP
- Prithvi-EO-2.0
- TerraTorch for Prithvi integration/fine-tuning where required

### Retrieval
- FAISS or Qdrant
- cosine/dot-product similarity

### Geospatial
- GDAL
- Rasterio
- GeoPandas
- Shapely
- pyproj

### Backend
- Python
- FastAPI

### Frontend
- React/Next.js or equivalent
- MapLibre GL JS/OpenLayers
- timeline and before/after viewer

### Database
- PostgreSQL + PostGIS

### Deployment
- Docker/Docker Compose
- local model weights
- local vector database
- local API/UI

---

## 22. Functional Requirements

**FR-01:** Ingest GeoTIFF/COG while preserving geospatial metadata.

**FR-02:** Generate standardized six-band Prithvi-compatible products.

**FR-03:** Perform quality assessment/masking.

**FR-04:** Generate and index semantic embeddings.

**FR-05:** Support natural-language retrieval.

**FR-06:** Support image-to-image retrieval.

**FR-07:** Support spatial, temporal and sensor/source filters.

**FR-08:** Analyse multiple observations for a candidate.

**FR-09:** Support the initial change categories.

**FR-10:** Estimate earliest usable observation supporting a change.

**FR-11:** Suppress specified pseudo-change factors.

**FR-12:** Produce confidence/uncertainty.

**FR-13:** Preserve source and processing provenance.

**FR-14:** Support analyst confirmation/rejection/uncertainty.

**FR-15:** Support incremental ingestion.

**FR-16:** Operate locally without external runtime APIs.

---

## 23. Non-Functional Requirements

- **Reproducibility:** versions/configuration must be recorded.
- **Explainability:** high-confidence changes must expose evidence.
- **Traceability:** every result maps to source scenes.
- **Scalability:** scene/tile growth must not require architectural redesign.
- **Offline operation:** no external inference/API dependency during evaluation.
- **Security:** imagery is not transmitted externally.
- **Fault isolation:** failed scenes can be reprocessed independently.

---

## 24. Evaluation

### Semantic retrieval
Measure:
- Recall@1;
- Recall@5;
- Recall@10;
- Mean Recall;
- mAP where appropriate.

Compare:
1. RemoteCLIP zero-shot;
2. India-adapted RemoteCLIP.

### Similar-site discovery
Measure relevance of retrieved sites.

### Change detection
Measure:
- Precision;
- Recall;
- F1;
- IoU;
- false-alarm rate.

### Change classification
Measure:
- per-class precision/recall;
- macro F1;
- confusion matrix.

### Earliest evidence
Compare predicted earliest supporting observation with labelled ground truth.

### Robustness
Evaluate under:
- seasonal differences;
- cloud contamination;
- haze;
- illumination variation;
- viewing-angle variation;
- registration perturbation;
- domain/sensor variation where available.

### System metrics
Record:
- scene/tile count;
- index build time;
- incremental ingestion time;
- query latency;
- model inference time;
- storage footprint;
- hardware used.

---

## 25. Baselines

### Baseline A
Metadata-only retrieval.

### Baseline B
RemoteCLIP zero-shot retrieval.

### Baseline C
Pixel/feature-difference change detection.

### Baseline D
Pretrained change-detection model without evidence fusion.

### Proposed system
RemoteCLIP + Prithvi + temporal semantic reasoning + evidence fusion + uncertainty.

---

## 26. Ablation Studies

Test the effect of:
1. India-domain adaptation;
2. temporal reasoning;
3. quality masks;
4. registration handling;
5. spectral evidence;
6. evidence fusion;
7. confidence calibration;
8. RemoteCLIP alone;
9. Prithvi alone;
10. hybrid architecture.

The goal is to prove which components improve retrieval and reduce false alarms.

---

## 27. Research Contribution

Do not claim novelty simply from using RemoteCLIP or Prithvi.

The research contribution should focus on the adaptation/inference problem:

> **India-domain semantic EO adaptation combined with multi-temporal semantic reasoning and uncertainty-aware evidence fusion for reliable change discovery under acquisition-induced pseudo-change.**

Potential contributions:
- Indian-domain semantic adaptation;
- semantic temporal-state modelling;
- evidence-aware change inference;
- confidence-aware analyst workflow;
- unified semantic discovery + temporal intelligence.

The exact novelty claim must be finalized after a systematic literature review.

---

## 28. End-to-End Example

### Query
> “Find locations with newly constructed facilities near major roads.”

### Pipeline

```text
Natural-language query
        ↓
RemoteCLIP semantic retrieval
        ↓
Top candidate locations
        ↓
Spatial/temporal metadata filtering
        ↓
Chronological observations
        ↓
Quality + registration processing
        ↓
Prithvi EO features
        ↓
Semantic temporal reasoning
        ↓
Construction/change classification
        ↓
Evidence fusion
        ↓
Confidence
        ↓
Analyst review
```

Example temporal interpretation:

```text
2022: Open land
2023: Open land
2024: Construction activity
2025: Permanent structure
2026: Permanent structure
```

Result:

```text
Change: New construction
Earliest supporting observation: 2024
Confidence: High
Evidence: multi-date semantic + spectral + temporal agreement
```

---

## 29. Failure Handling

### Cloud-contaminated observation
Mask affected areas and reduce usable evidence.

### Poor registration
Flag/exclude observation or reduce confidence.

### Seasonal variation
Use multi-date evidence and reduce confidence where ambiguity remains.

### Sensor/domain mismatch
Use sensor metadata and compatible normalization; reduce confidence if unsupported.

### Insufficient observations
Return “insufficient evidence” rather than forcing a decision.

### Conflicting evidence
Return “ambiguous” and expose the evidence to the analyst.

---

## 30. Security and Sovereignty

- imagery remains on-premises;
- model weights are staged locally;
- no external inference API;
- no external image upload;
- network can be disabled after staging;
- processing logs should avoid unnecessary sensitive imagery content;
- access should be role-controlled in production;
- exported results must retain provenance.

---

## 31. Model Provenance

For each model store:

```text
model_name
model_version
source
license
checkpoint_hash
input_bands
input_resolution
normalization
fine_tuning_dataset
fine_tuning_version
inference_config
```

All pretrained public models must have declared origin/licence and packaged weights for offline use.

---

## 32. Data Provenance

For each scene:

```text
scene_id
source
product_id
sensor
acquisition_datetime
processing_level
CRS
bounds
resolution
band_list
quality_metadata
```

For each derived tile:

```text
parent_scene_id
tile_id
window
transform
CRS
preprocessing_version
quality_mask_version
```

---

## 33. MVP

The minimum demonstration shall include:

1. Sentinel-2 L2A data from multiple Indian AOIs.
2. Multi-year temporal observations.
3. Standardized six-band GeoTIFF/COG processing.
4. RemoteCLIP semantic search.
5. Image-to-image retrieval.
6. Prithvi-EO-2.0 feature extraction.
7. Multi-temporal change analysis for selected categories.
8. Quality masking and registration.
9. Evidence fusion.
10. Confidence score.
11. Before/after/timeline UI.
12. Provenance.
13. Incremental ingestion.
14. Offline local inference.

---

## 34. Implementation Phases

### Phase 1 — Data Foundation
AOIs → Sentinel-2 L2A → staging → GeoTIFF/COG → metadata → quality masks.

### Phase 2 — Semantic Retrieval
RemoteCLIP → RGB tiles → embeddings → vector index → text/image retrieval → Indian benchmark.

### Phase 3 — EO Representation
Prithvi-EO-2.0-300M-TL → six-band standardization → temporal features.

### Phase 4 — Temporal Intelligence
Custom temporal head → change localization → classification → earliest evidence.

### Phase 5 — Reliability
Evidence fusion → uncertainty → false-alarm evaluation → ablations.

### Phase 6 — Analyst Workflow
Map → timeline → before/after → evidence → provenance → feedback.

### Phase 7 — Offline Deployment
Package models → containerize → disable network → benchmark latency/storage/hardware.

---

## 35. Final Architecture Decision

The division of responsibility shall be:

```text
REMOTECLIP
    ↓
Language ↔ imagery semantic alignment
    ↓
PRITHVI-EO-2.0
    ↓
Multispectral + temporal EO representation
    ↓
OUR TEMPORAL MODEL
    ↓
Semantic state transitions
    ↓
OUR EVIDENCE FUSION
    ↓
Pseudo-change suppression
    ↓
OUR CONFIDENCE LAYER
    ↓
Evidence-ranked analyst review
```

One model must not be expected to solve retrieval, EO representation, temporal reasoning and uncertainty simultaneously.

---

## 36. Critical Compatibility Note

The documented Prithvi-EO-2.0 six-band HLS configuration uses:

```text
B02, B03, B04, B05, B06, B07
```

Therefore, the Sentinel-2 preprocessing pipeline must explicitly map these bands to the Prithvi-compatible six-band representation.

The commonly used Sentinel-2 combination:

```text
B02, B03, B04, B08, B11, B12
```

must not be presented as the exact six-band Prithvi input.

---

## 37. Product Positioning

EDGE-GEOINT is not merely:
- a satellite image search engine; or
- a change-detection model.

It is an integrated intelligence workflow:

> **Semantic discovery → candidate retrieval → multi-temporal semantic reasoning → pseudo-change suppression → confidence estimation → analyst validation → provenance-preserving intelligence.**

### Central research hypothesis

> **Reliable satellite-image intelligence can be improved by combining remote-sensing foundation-model representations with temporal semantic reasoning and uncertainty-aware evidence fusion, rather than relying on metadata search or pixel-by-pixel change alone.**

---

## 38. Technical References

- SIH Problem Statement: Semantic Retrieval and Multi-Temporal Change Analysis of Satellite Imagery.
- RemoteCLIP: A Vision Language Foundation Model for Remote Sensing.
- Prithvi-EO-2.0: Earth Observation Foundation Model.
- Recent foundation-model-driven semantic change detection research.
- Sentinel-2 Level-2A product documentation.

All final experiments must record exact model/checkpoint versions, dataset versions, preprocessing configurations and licenses.
