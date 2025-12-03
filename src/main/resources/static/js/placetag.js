let places = [];
let tags = [];
let selectedPlaces = [];

async function loadData() {
    // 태그 리스트
    const tagRes = await fetch("/api/admin/tags");
    const tagJson = await tagRes.json();
    tags = tagJson.data;

    // 장소 + 태그 리스트
    const placeRes = await fetch("/api/admin/places/tags");
    const placeJson = await placeRes.json();
    places = placeJson.data;

    renderPlaces();
}

window.onload = () => {
  loadData();
};


const container = document.getElementById("places-container");
const modal = document.getElementById("tagModal");
const modalTags = document.getElementById("modal-tags");

// 랜더링
function renderPlaces() {
  container.innerHTML = "";
  places.forEach(place => {
    const card = document.createElement("div");
    card.className = "place-card";

    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.onchange = (e) => {
      if(e.target.checked) selectedPlaces.push(place.id);
      else selectedPlaces = selectedPlaces.filter(id => id !== place.id);
    };

    const title = document.createElement("div");
    title.className = "place-title";
    title.textContent = place.name;

    const tagContainer = document.createElement("div");
    tagContainer.className = "tags";

    // 태그 객체로 변경됨
    place.tags.forEach(tag => {
      const tagEl = document.createElement("div");
      tagEl.className = "tag";
      tagEl.textContent = tag.name; // name 표시

      const removeSpan = document.createElement("span");
      removeSpan.textContent = "✖";
      removeSpan.onclick = () => removeTag(place.id, tag.id); // id로 삭제
      tagEl.appendChild(removeSpan);

      tagContainer.appendChild(tagEl);
    });

    card.appendChild(checkbox);
    card.appendChild(title);
    card.appendChild(tagContainer);

    container.appendChild(card);
  });
}


function removeTag(placeId, tagId) {
  fetch(`/api/admin/places/${placeId}/tags/${tagId}`, {
    method: "DELETE"
  })
  .then(res => {
    if(!res.ok) throw new Error("삭제 실패");

    const place = places.find(p => p.id === placeId);
    place.tags = place.tags.filter(t => t.id !== tagId); // id로 제거
    renderPlaces();
  })
  .catch(err => {
    console.error(err);
    alert("삭제 중 오류 발생");
  });
}



// 모달 열기
document.getElementById("bulkAddBtn").onclick = async () => {
  // tags가 아직 로드되지 않았다면 다시 fetch
  if(!Array.isArray(tags) || tags.length === 0){
    const tagRes = await fetch("/api/admin/tags");
    const tagJson = await tagRes.json();
    tags = tagJson.data;
  }

  if(selectedPlaces.length === 0){
    alert("태그를 추가할 장소를 선택해주세요.");
    return;
  }
  modalTags.innerHTML = "";
  tags.forEach(tag => {
    const label = document.createElement("label");
    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.value = tag;
    label.appendChild(checkbox);
    label.appendChild(document.createTextNode(" " + tag));
    modalTags.appendChild(label);
  });
  modal.style.display = "flex";
}
// ----------------------
// 모달 저장 로직 수정
// ----------------------
document.getElementById("saveTags").onclick = async () => {
  const checkedTags = Array.from(modalTags.querySelectorAll("input:checked"))
    .map(cb => cb.value);

  if (checkedTags.length === 0) {
    alert("추가할 태그를 선택해주세요.");
    return;
  }

  const payload = {
    placeIds: selectedPlaces,
    tags: checkedTags
  };

  try {
    const res = await fetch("/api/admin/places/tags", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const json = await res.json();
    if (!json.success) throw new Error("태그 저장 실패");

    const savedTags = json.data;
    // [{ placeId:1, id:101, name:"카페" }, ... ]

    selectedPlaces.forEach(placeId => {
      const place = places.find(p => p.id === placeId);
      place.tags = place.tags || [];

      const newTagsForPlace = savedTags.filter(t => t.placeId === placeId);
      newTagsForPlace.forEach(tag => {
        if (!place.tags.some(t => t.id === tag.id)) {
          place.tags.push(tag);
        }
      });
    });

    modal.style.display = "none";
    renderPlaces();
    alert("태그가 성공적으로 저장되었습니다.");

  } catch (err) {
    console.error(err);
    alert("태그 저장 중 오류가 발생했습니다.");
  }
};

// 모달 닫기 (배경 클릭)
window.onclick = (event) => {
  if (event.target === modal) modal.style.display = "none";
}

