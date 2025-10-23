#!/usr/bin/env bash
set -euo pipefail

REGISTRY_URL="http://localhost:8081/apis/registry/v2"
GROUP=${GROUP:-shipment}
CONTENT_TYPE="application/json"
ARTIFACT_TYPE="AVRO"
ON_EXISTING=${ON_EXISTING:-skip}

exists_artifact() {
  local artifact_id="$1"
  local code
  code=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Accept: application/json" \
    "${REGISTRY_URL}/groups/${GROUP}/artifacts/${artifact_id}")
  [[ "$code" == "200" ]]
}

create_artifact() {
  local artifact_id="$1"; local file_path="$2"
  echo "➕ Creating artifact: ${artifact_id}  (from ${file_path})"
  curl -s -X POST \
    -H "Content-Type: ${CONTENT_TYPE}" \
    -H "X-Registry-ArtifactType: ${ARTIFACT_TYPE}" \
    -H "X-Registry-ArtifactId: ${artifact_id}" \
    --data-binary @"${file_path}" \
    "${REGISTRY_URL}/groups/${GROUP}/artifacts" | jq '.id,.version' >/dev/null
}

new_version() {
  local artifact_id="$1"; local file_path="$2"
  echo "🔁 Adding new version to: ${artifact_id}  (from ${file_path})"
  curl -s -X POST \
    -H "Content-Type: ${CONTENT_TYPE}" \
    --data-binary @"${file_path}" \
    "${REGISTRY_URL}/groups/${GROUP}/artifacts/${artifact_id}/versions" | jq '.id,.version' >/dev/null
}

ensure_artifact() {
  local artifact_id="$1"; local file_path="$2"
  if exists_artifact "${artifact_id}"; then
    case "${ON_EXISTING}" in
      skip)    echo "✔ Exists, skipping: ${artifact_id}";;
      version) new_version "${artifact_id}" "${file_path}";;
      *) echo "Unknown ON_EXISTING='${ON_EXISTING}', use 'skip' or 'version'"; exit 1;;
    esac
  else
    create_artifact "${artifact_id}" "${file_path}"
  fi
}

declare -A SCHEMAS=(
  # shipment-events
  ["ShipmentCreated"]="../schemas/avro/shipment-events/ShipmentCreated.avsc"
  ["ReturnInitiated"]="../schemas/avro/shipment-events/ReturnInitiated.avsc"
  # shipment-tracking
  ["ShipmentTrackingState"]="../schemas/avro/shipment-tracking/ShipmentTrackingState.avsc"
  # tracking-updates
  ["TrackingUpdated"]="../schemas/avro/tracking-updates/TrackingUpdated.avsc"
)

echo "Apicurio Registry: ${REGISTRY_URL}"
echo "Group:            ${GROUP}"
echo "On existing:      ${ON_EXISTING}"
echo

for artifact_id in "${!SCHEMAS[@]}"; do
  file_path="${SCHEMAS[$artifact_id]}"
  if [[ ! -f "${file_path}" ]]; then
    echo "❌ Missing file: ${file_path}  (artifact ${artifact_id})" >&2
    exit 2
  fi
done

for artifact_id in "${!SCHEMAS[@]}"; do
  ensure_artifact "${artifact_id}" "${SCHEMAS[$artifact_id]}"
done

echo
echo "✅ Done."