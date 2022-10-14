echo script name:
read -r scriptname

timestamp="$(date +%s)"
filename="V${timestamp}__${scriptname}.sql"
filepath="ebegu-dbschema/src/main/resources/db/migration/"

echo "# TODO insert copyright" >> "$filepath$filename"

git add "$filepath$filename"
