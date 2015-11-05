/*
 * Copyright 2014 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.packaging.gui;

import java.util.ResourceBundle;

/**
 * Wrapper to access bundled resource for labels.
 */
public class Labels {

    public enum LabelKey {
        CANCEL_BUTTON("cancel.button"),
        OPEN_PACKAGE_DESCRIPTION_LABEL_KEY("openpackagedescription.label"),
        PACKAGE_DESCRIPTION_FILE_CHOOSER_KEY("packagedescriptionfile.save"),
        OUTPUT_DIRECTORY_CHOOSER_KEY("outputdirectory.chooser"),
        OUTPUT_DIRECTORY_LABEL_KEY("outputdirectory.label"),
        PACKAGE_ARTIFACT_CREATOR("packageartifact.creator"),
        PACKAGE_ARTIFACT_GENERAL("packageartifact.general"),
        PACKAGE_ARTIFACT_RELATIONSHIPS("packageartifact.relationships"),
        PACKAGE_ARTIFACT_INHERITANCE("packageartifact.inheritance"),
        METADATA_INHERITANCE_LABEL("metadatainheritance.label"),
        ENABLED_LABEL("enabled.label"),
        DISABLED_LABEL("disabled.label"),
        COLLECTION_DISCIPLINE("Collection.discipline"),
        COLLECTION_SUMMARY("Collection.Summary"),
        COLLECTION_CREATORS("collection.creator.label"),
        COLLECTION_CONTACT_INFO("collection.contactinfo.label"),
        APPLY_INHERITED_METADATA_BUTTON("applyinheritedmetadata.button"),
        SHOW_FULL_PATHS("showfullpaths.label"),
        CREATE_PACKAGE_PAGE("createPackage.page"),
        DEFINE_RELATIONSHIPS_PAGE("defineRelationships.page"),
        GENERATE_PACKAGE_PAGE("generatePackage.page"),
        CONTINUE_BUTTON("continue.button"),
        HELP_LABEL("help.label"),
        ABOUT_LABEL("about.label"),
        SAVE_AND_CONTINUE_BUTTON("saveandcontinue.button"),
        FINISH_BUTTON("finish.button"),
        NO_THANKS_LINK("nothanks.link"),
        CREATE_ANOTHER_PACKAGE_BUTTON("createanotherpackage.button"),
        PACKAGE_LABEL("package.label"),
        PACKAGE_NAME_LABEL("packagename.label"),
        EMAIL_LABEL("email.label"),
        NAME_LABEL("name.label"),
        PHONE_LABEL("phone.label"),
        CONTACT_LABEL("contact.label"),
        BASE_DIRECTORY_LABEL("basedirectory.label"),
        BROWSE_BUTTON("browse.button"),
        BROWSEDIR_BUTTON("browsedir.button"),
        ARCHIVE_FORMAT_LABEL("archiveformat.label"),
        COMPRESSION_FORMAT_LABEL("compressionformat.label"),
        TAR_BUTTON("tar.button"),
        ZIP_BUTTON("zip.button"),
        GZIP_BUTTON("gzip.button"),
        EXPLODED_BUTTON("exploded.button"),
        SUCCESS_LABEL("success.label"),
        ANOTHER_FORMAT_LABEL("anotherformat.label"),
        RELATIONSHIP_DEFINITION_LABEL("relationshipdefinition.label"),
        UTC_HINT("utchint.label"),
        SAVE_BUTTON("save.button"),
        APPLY_BUTTON("apply.button"),
        OK_BUTTON("ok.button"),
        BACK_LINK("back.link"),
        OR_LABEL("or.label"),
        BASIC_HELP_TITLE("basichelptitle.label"),
        BASIC_HELP_TEXT("basichelptext.label"),
        BASIC_ABOUT_TITLE("basicabouttitle.label"),
        BASIC_ABOUT_TEXT("basicabouttext.label"),
        ADD_NEW_BUTTON("addnew.button"),
        ADD_RELATIONSHIP_BUTTON("addrelationship.button"),
        NONE_LABEL("none.label"),
        CHECKSUM_LABEL("checksum.label"),
        MD5_CHECKBOX("md5.checkbox"),
        SHA1_CHECKBOX("sha1.checkbox"),
        HIERARCHICAL_ADVICE_LABEL("hierarchical.label"),
        PACKAGING_OPTIONS_LABEL("packagingoptions.label"),
        BUILD_NUMBER_LABEL("buildnumber.label"),
        BUILD_REVISION_LABEL("buildrevision.label"),
        BUILD_TIMESTAMP_LABEL("buildtimestamp.label"),
        APPLY_FIELD_INHERITANCE_BUTTON("applyInheritance.button"),
        INHERITANCE_TAB_INTRO("inheritanceTabIntro.label"),
        INHERITANCE_BUTTON_EXPLAINED("inheritanceButtonExplained.label"),
        INHERITANCE_DESCENDANT_TYPE("inheritanceDescendantType.label"),
        NO_INHERITABLE_PROPERTY("noinheritableproperty.label"),
        PROPERTY_INPUT_LABEL("user.property.input.label"),
        EXTERNAL_IDENTIFIER_LABEL_KEY("externalidentifier.label"),
        RELATIONSHIP_NAME_HINT("relationshipnamehint.label"),
        FILE_EXISTS_WARNING_TITLE_LABEL("fileexistswarningtitle.label"),
        FILE_EXISTS_WARNING_TEXT_LABEL("fileexistswarningtext.label"),
        DONT_SHOW_WARNING_AGAIN_CHECKBOX("dontshowwarningagain.checkbox"),
        RENABLE_PROPERTY_WARNINGS_BUTTON("reenablepropertywarnings.button"),
        PROGRESS_INDICATOR("progressindicator.label"),
        BUILDING_PACKAGE_DESCRIPTION("packagedescriptionbuilding.label"),
        PROPERTIES_LABEL("properties.label"),
        KEEP_TYPE_LABEL("keeptype.label"),
        CHANGE_TYPE_LABEL("changetype.label"),
        NAMESPACE_LABEL("namespace.label"),
        RELATIONSHIP_LABEL("relationship.label"),
        REQURIRES_URI_LABEL("requiresuri.label"),
        RELATIONSHIP_MUST_BE_URI_OR_KNOWN("mustbeuriorknown.label"),
        RELATIONSHIP_TARGET_LABEL("relationshiptarget.label"),
        URI_LABEL("uri.label"),
        LITERAL_LABEL("literal.label"),
        EXTERNAL_PROJECT_LABEL("externalproject.label"),
        IGNORE_CHECKBOX("ignore.checkbox"),
        GENERATING_PACKAGE_LABEL("generatingpackage.label"),
        SYNTHESIZED_ARTIFACT_NOTATION("synthesizedartifact.label"),
        SHOW_FULL_PATHS_TIP("showfullpaths.tip"),
        SHOW_IGNORED("showignored.label"),
        SHOW_IGNORED_TIP("showignored.tip"),
        PACKAGE_DIRECTORY_LABEL("packagedirectory.label"),
        SELECT_IN_PROGRESS_PACKAGE_FILE_LABEL("inprogresspackagefile.label"),
        CREATE_NEW_PACKAGE("createnewpackage.label"),
        OPEN_EXISTING_PACKAGE("openexistingpackage.label"),
        HOMEPAGE_PAGE("homepage.page"),
        SELECT_DOMAIN_PROFILE_LABEL("selectdomainprofile.label"),
        ADD_BUTTON("add.button"),
        KEYWORD_LABEL("keyword.label"),
        EXTERNAL_DESCRIPTION_LABEL("externaldescritption.label"),
        INTERNAL_SENDER_IDENTIFIER_LABEL("internalsenderidentifier.label"),
        INTERNAL_DESCRIPTION_LABEL("internaldescritption.label"),
        SOURCE_ORGANIZATION_LABEL("sourceorganization.label"),
        ORGANIZATION_ADDRESS_LABEL("organizatoinaddress.label"),
        BAG_COUNT_LABEL("bagcount.label"),
        BAG_GROUP_IDENTIFIER_LABEL("baggroupidentifier.label"),
        RIGHTS_LABEL("rights.label"),
        RIGHTS_URI_LABEL("rightsuri.label"),
        BAGGING_DATE_LABEL("baggingdate.label"),
        BAG_SIZE_LABEL("bagsize.label"),
        PACKAGE_METADATA("packagemetadata.label"),
        SERIALIZATION_FORMAT_LABEL("serialization.label"),
        JSON_BUTTON("json.button"),
        XML_BUTTON("xml.button"),
        TURTLE_BUTTON("turtle.button"),
        SELECT_PACKAGE_FILE_LABEL("selectpackagefile.label"),
        REQUIRED_FIELDS_LABEL("requiredfields.label"),
        RECOMMENDED_FIELDS_LABEL("recommendedfields.label"),
        OPTIONAL_FIELDS_LABEL("optionalfields.label"),
        TYPE_VALUE_AND_ENTER_PROMPT("typevalueandenter.prompt"),
        PACKAGE_OUTPUT_DIRECTORY_LABEL("packageoutputdirectory.label"),
        SELECT_ONE_OPTION_LABEL("selectoneoption.label"),
        WARNING_POPUP_TITLE("warningpopup.title"),
        ALL_FIELDS_CLEAR_WARNING_MESSAGE("allfieldsclearwarning.message"),
        ADD_ITEM_LABEL("additem.label"),
        ADD_FILE_ITEM_LABEL("addfileitem.label"),
        ADD_FOLDER_ITEM_LABEL("addfolderitem.label"),
        REFRESH_ITEM_LABEL("refreshitem.label"),
        REMAP_ITEM_LABEL("remapitem.label"),
        REMAP_FILE_ITEM_LABEL("remapfileitem.label"),
        REMAP_FOLDER_ITEM_LABEL("remapfolderitem.label"),
        ACCEPT_BUTTON("accept.button"),
        REJECT_BUTTON("reject.button"),
        DETECTED_CHANGES_LABEL("detectedchanges.label");

        private String property;

        LabelKey(String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
        }
    }

    private ResourceBundle bundle;

    public Labels(ResourceBundle bundle) {
        this.bundle = bundle;

        for (LabelKey key : LabelKey.values()) {
            if (!bundle.containsKey(key.getProperty())) {
                throw new IllegalArgumentException("Missing resource in bundle: " + key.getProperty());
            }
        }
    }

    public String get(LabelKey key) {
        if (!bundle.containsKey(key.getProperty())) {
            throw new IllegalArgumentException("No such resource: " + key.getProperty());
        }

        return bundle.getString(key.getProperty());
    }

    public String format(LabelKey key, Object... args) {
        if (!bundle.containsKey(key.property)) {
            throw new IllegalArgumentException("No such resource: " +
                                                   key);
        }

        return String.format(bundle.getLocale(), bundle.getString(key.property), args);
    }
}
