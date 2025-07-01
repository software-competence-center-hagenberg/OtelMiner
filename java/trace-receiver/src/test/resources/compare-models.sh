#!/usr/bin/env bash

FILE1=$1
FILE2=$2
THRESHOLD=$3

if [[ ! -f "$FILE1" || ! -f "$FILE2" ]]; then
  echo "Usage: $0 file1.json file2.json"
  exit 1
fi

extract_constraints() {
  jq -S '
    .declareList // [] |
    sort_by(.constraintTemplate) |
    map({
      constraintTemplate,
      probability,
      nr
    })
  ' "$1"
}

TMP1=$(mktemp)
TMP2=$(mktemp)

extract_constraints "$FILE1" > "$TMP1"
extract_constraints "$FILE2" > "$TMP2"

# Build sets for comparison
ONLY_IN_FILE1=$(mktemp)
ONLY_IN_FILE2=$(mktemp)
COMMON=$(mktemp)

# Extract sorted template names for comparison only
jq -r '.declareList[].constraintTemplate' "$FILE1" | sort > "$TMP1.templates"
jq -r '.declareList[].constraintTemplate' "$FILE2" | sort > "$TMP2.templates"

comm -23 "$TMP1.templates" "$TMP2.templates" > "$ONLY_IN_FILE1"
comm -13 "$TMP1.templates" "$TMP2.templates" > "$ONLY_IN_FILE2"
comm -12 "$TMP1.templates" "$TMP2.templates" > "$COMMON"

changed_count=0
unchanged_count=0

echo ""
echo "Differences in common constraints (based on 'constraintTemplate')"
printf "%-25s | %-10s | %-10s | %-10s | %-10s\n" "DeclareTemplate" "Prob1" "Prob2" "Nr1" "Nr2"
printf "%s\n" "-------------------------+------------+------------+------------+------------"

while read -r template; do
  val1=$(jq -r --arg t "$template" '.declareList[] | select(.constraintTemplate == $t)' "$FILE1" | jq -s '.[0]')
  val2=$(jq -r --arg t "$template" '.declareList[] | select(.constraintTemplate == $t)' "$FILE2" | jq -s '.[0]')

  prob1=$(echo "$val1" | jq '.probability')
  prob2=$(echo "$val2" | jq '.probability')
  nr1=$(echo "$val1" | jq '.nr')
  nr2=$(echo "$val2" | jq '.nr')

  
  diff=$(echo "$prob1 - $prob2" | bc -l)
  abs_diff=$(echo "$diff" | awk '{print ($1 < 0) ? -$1 : $1}')

  # Check if different and above threshold
  if [[ "$nr1" != "$nr2" || $(echo "$abs_diff > $THRESHOLD" | bc -l) -eq 1 ]]; then
    ((changed_count++))
    printf "%-25s | %-10s | %-10s | %-10s | %-10s\n" "$template" "$prob1" "$prob2" "$nr1" "$nr2"
  else
    ((unchanged_count++))
  fi
done < "$COMMON"

print_file_only_count() {
  local file=$1
  local only_in_file=$2
  local count=$(wc -l < "$only_in_file")
  echo ""
  echo "file $file contains $count constraints that only occur here"
}

print_file_only_constraints() {
  local file=$1
  local only_in_file=$2
  echo ""
  echo "Constraints only in file ($file):"
  if [[ -s $only_in_file ]]; then
    while read -r template; do
      jq --arg t "$template" '.declareList[] | select(.constraintTemplate == $t)' "$file"
    done < "$only_in_file"
  else
    echo "None"
  fi
}

echo ""
echo "Common constraints minor changes (< $THRESHOLD): $unchanged_count"
echo "Common constraints changed (> $THRESHOLD):   $changed_count"

print_file_only_count "$FILE1" "$ONLY_IN_FILE1"
print_file_only_count "$FILE2" "$ONLY_IN_FILE2"
print_file_only_constraints "$FILE1" "$ONLY_IN_FILE1"
print_file_only_constraints "$FILE2" "$ONLY_IN_FILE2"

# Cleanup
rm "$TMP1" "$TMP2" "$TMP1.templates" "$TMP2.templates" "$ONLY_IN_FILE1" "$ONLY_IN_FILE2" "$COMMON"
